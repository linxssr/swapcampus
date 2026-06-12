package com.campus.user.dto;

public class UserInfoResponse {
    private Long userId;
    private String studentId;
    private String username;
    private String phone;
    private String avatar;
    private Integer creditScore;
    private Integer status;
    private String createTime;

    // 构造方法
    public UserInfoResponse(Long userId, String studentId, String username,
                            String phone, String avatar, Integer creditScore,
                            Integer status, String createTime) {
        this.userId = userId;
        this.studentId = studentId;
        this.username = username;
        this.phone = phone;
        this.avatar = avatar;
        this.creditScore = creditScore;
        this.status = status;
        this.createTime = createTime;
    }

    // getter 和 setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}