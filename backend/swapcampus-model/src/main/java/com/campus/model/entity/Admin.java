package com.campus.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Admin {
    private Long adminId;
    private String adminName;
    private String password;
    private String realName;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
}

