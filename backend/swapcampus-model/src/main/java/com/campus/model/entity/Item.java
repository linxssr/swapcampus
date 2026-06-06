package com.campus.model.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Item {
    private Long itemId;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer quality;
    private String coverUrl;
    private Integer auditStatus;
    private Long auditAdminId;
    private Integer publishStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

