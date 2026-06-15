package com.campus.user.service;

import com.campus.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

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

    // 批量获取订单的商品名称
    List<Map<String, Object>> getOrderItems(List<Long> orderIds);

    // 批量获取商品信息
    List<Map<String, Object>> getItemsByIds(List<Long> itemIds);

    // 上传头像
    String uploadAvatar(Long userId, MultipartFile file);

    List<Map<String, Object>> getUserList(String keyword, Integer status, int offset, int size);

    int countUserList(String keyword, Integer status);

    // 签到
    Map<String, Object> sign(Long userId);

    // 获取签到状态
    Map<String, Object> getSignStatus(Long userId);

    // 根据学号获取真实姓名
    String getRealNameByStudentId(String studentId);

    // 检查学号是否在对照表中存在
    boolean checkStudentIdExists(String studentId);

}