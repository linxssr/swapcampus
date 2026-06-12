package com.campus.user.dto;

public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private Integer creditScore;

    // 构造方法
    public LoginResponse(String token, Long userId, String username, Integer creditScore) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.creditScore = creditScore;
    }

    // getter 和 setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }
}