package com.campus.order.service.impl;

import com.campus.common.exception.BusinessException;
import com.campus.mapper.ItemMapper;
import com.campus.mapper.OrderMapper;
import com.campus.model.entity.Item;
import com.campus.model.entity.Order;
import com.campus.order.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private ItemMapper itemMapper;

    @Override
    @Transactional
    public String createOrder(Long buyerId, Long itemId, Integer tradeType, BigDecimal price) {
        Item item = itemMapper.selectById(itemId);
        if (item == null || item.getPublishStatus() == 0) {
            throw new BusinessException(400, "商品已下架或不存在");
        }
        
        if (item.getAuditStatus() != 1) {
            throw new BusinessException(400, "商品未通过审核");
        }

        if (buyerId.equals(item.getUserId())) {
            throw new BusinessException(400, "不能购买自己发布的商品");
        }

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long todayOrderCount = orderMapper.countOrdersByDate(dateStr);
        String serialNum = String.format("%04d", todayOrderCount + 1);
        String orderNo = "ORD" + dateStr + serialNum;

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setBuyerId(buyerId);
        order.setSellerId(item.getUserId());
        order.setItemId(itemId);
        order.setPrice(price);
        order.setTradeType(tradeType);
        order.setOrderStatus(1);
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);

        item.setPublishStatus(0);
        itemMapper.updateById(item);

        return orderNo;
    }

    @Override
    public Order getOrderByNo(String orderNo, Long currentUserId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        if (!order.getBuyerId().equals(currentUserId) && !order.getSellerId().equals(currentUserId)) {
            throw new BusinessException(403, "无权查看此订单");
        }

        return order;
    }

    @Override
    public List<Map<String, Object>> getMyOrders(Long userId, String type) {
        if ("buy".equals(type)) {
            return orderMapper.selectBuyerOrders(userId);
        } else if ("sell".equals(type)) {
            return orderMapper.selectSellerOrders(userId);
        } else {
            throw new BusinessException(400, "type参数必须是 buy 或 sell");
        }
    }

    @Override
    @Transactional
    public void confirmOrder(String orderNo, Long currentUserId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        if (!order.getSellerId().equals(currentUserId)) {
            throw new BusinessException(403, "只有卖家可以确认订单");
        }

        if (order.getOrderStatus() != 1) {
            throw new BusinessException(400, "当前订单状态无法确认");
        }

        orderMapper.updateOrderStatus(orderNo, 2);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNo, Long currentUserId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        if (!order.getBuyerId().equals(currentUserId) && !order.getSellerId().equals(currentUserId)) {
            throw new BusinessException(403, "无权操作此订单");
        }

        if (order.getOrderStatus() != 1 && order.getOrderStatus() != 2) {
            throw new BusinessException(400, "当前订单状态无法取消");
        }

        orderMapper.updateOrderStatus(orderNo, 4);

        Item item = itemMapper.selectById(order.getItemId());
        if (item != null) {
            item.setPublishStatus(1);
            itemMapper.updateById(item);
        }
    }

    @Override
    @Transactional
    public void finishOrder(String orderNo, Long currentUserId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        if (!order.getBuyerId().equals(currentUserId)) {
            throw new BusinessException(403, "只有买家可以确认收货");
        }

        if (order.getOrderStatus() != 2) {
            throw new BusinessException(400, "当前订单状态无法确认收货");
        }

        orderMapper.updateOrderStatus(orderNo, 3);
        orderMapper.updateFinishTime(orderNo, LocalDateTime.now());
    }
}
