package com.campus.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Report {
    private Long reportId;
    private Long reportUser;
    private Long itemId;
    private String reason;
    private Integer handleStatus;
    private Long handleAdminId;
    private String handleResult;
    private LocalDateTime createTime;
}

