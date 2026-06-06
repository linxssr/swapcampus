package com.campus.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Long commentId;
    private Long orderId;
    private Long buyerId;
    private Long sellerId;
    private Long itemId;
    private String content;
    private Integer score;
    private LocalDateTime createTime;
}

