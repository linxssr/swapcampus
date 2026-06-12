package com.campus.user.dto;

public class PasswordUpdateRequest {
    private String oldPassword;
    private String newPassword;

    // getter 和 setter
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}