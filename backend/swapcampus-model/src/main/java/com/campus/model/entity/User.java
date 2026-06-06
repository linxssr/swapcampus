package com.campus.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long userId;
    private String studentId;
    private String username;
    private String password;
    private String phone;
    private String avatar;
    private Integer creditScore;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

