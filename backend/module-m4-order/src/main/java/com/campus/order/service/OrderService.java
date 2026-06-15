package com.campus.order.service;

import com.campus.model.entity.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderService {

    String createOrder(Long buyerId, Long itemId, Integer tradeType, BigDecimal price);

    Order getOrderByNo(String orderNo, Long currentUserId);

    List<Map<String, Object>> getMyOrders(Long userId, String type);

    void cancelOrder(String orderNo, Long currentUserId);

    void finishOrder(String orderNo, Long currentUserId);
}
