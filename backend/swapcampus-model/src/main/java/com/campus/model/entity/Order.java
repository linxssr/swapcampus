package com.campus.model.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long orderId;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long itemId;
    private BigDecimal price;
    private Integer tradeType;
    private Integer orderStatus;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}

