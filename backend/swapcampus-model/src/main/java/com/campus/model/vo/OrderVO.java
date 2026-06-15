package com.campus.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    private Long orderId;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long itemId;
    private String itemTitle;
    private String itemCover;
    private String peerName;
    private BigDecimal price;
    private Integer tradeType;
    private Integer orderStatus;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
