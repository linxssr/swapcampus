package com.campus.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CollectVO {
    private Long collectId;
    private Long userId;
    private Long itemId;
    private LocalDateTime createTime;
    private ItemVO item;
}
