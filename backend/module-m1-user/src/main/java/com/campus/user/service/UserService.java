package com.campus.user.service;

import com.campus.model.entity.User;
import java.util.List;
import java.util.Map;

public interface UserService {

    boolean register(User user);

    User login(String studentId, String password);

    User getUserInfo(Long userId);

    List<Map<String, Object>> getMyItems(Long userId);

    List<Map<String, Object>> getMyOrders(Long userId, String type);

    // ========== 新增这两个方法声明 ==========

    boolean updateUserInfo(Long userId, String username, String phone, String avatar);

    boolean updatePassword(Long userId, String oldPassword, String newPassword);
}