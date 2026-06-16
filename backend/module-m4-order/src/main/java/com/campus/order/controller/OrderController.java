package com.campus.order.controller;

import com.campus.common.result.Result;
import com.campus.common.utils.JwtUtil;
import com.campus.model.entity.Order;
import com.campus.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody Map<String, Object> requestData,
                                      HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "请先登录");
        }

        Long buyerId = jwtUtil.getUserIdFromToken(token);
        Long itemId = Long.valueOf(requestData.get("itemId").toString());
        Integer tradeType = Integer.valueOf(requestData.get("tradeType").toString());
        BigDecimal price = new BigDecimal(requestData.get("price").toString());

        String orderNo = orderService.createOrder(buyerId, itemId, tradeType, price);
        return Result.success(orderNo, "下单成功");
    }

    @GetMapping("/{oid}")
    public Result<Order> getOrderById(@PathVariable("oid") String oid,
                                      HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "请先登录");
        }

        Long currentUserId = jwtUtil.getUserIdFromToken(token);
        Order order = orderService.getOrderByNo(oid, currentUserId);
        return Result.success(order);
    }

    @PutMapping("/confirm/{oid}")
    public Result<Void> confirmOrder(@PathVariable("oid") String oid,
                                     HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "请先登录");
        }

        Long currentUserId = jwtUtil.getUserIdFromToken(token);
        orderService.confirmOrder(oid, currentUserId);
        return Result.success(null, "订单已确认");
    }

    @PutMapping("/cancel/{oid}")
    public Result<Void> cancelOrder(@PathVariable("oid") String oid,
                                    HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "请先登录");
        }

        Long currentUserId = jwtUtil.getUserIdFromToken(token);
        orderService.cancelOrder(oid, currentUserId);
        return Result.success(null, "订单已取消");
    }

    @PutMapping("/finish/{oid}")
    public Result<Void> finishOrder(@PathVariable("oid") String oid,
                                    HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "请先登录");
        }

        Long currentUserId = jwtUtil.getUserIdFromToken(token);
        orderService.finishOrder(oid, currentUserId);
        return Result.success(null, "确认收货成功");
    }
}
