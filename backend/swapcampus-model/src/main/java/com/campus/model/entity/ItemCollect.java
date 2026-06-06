package com.campus.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemCollect {
    private Long collectId;
    private Long userId;
    private Long itemId;
    private LocalDateTime createTime;
}

