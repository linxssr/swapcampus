package com.campus.user.service.impl;

import com.campus.mapper.UserMapper;
import com.campus.model.entity.User;
import com.campus.user.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
//chcp 65001
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 构造方法注入
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public boolean register(User user) {
        // 1. 检查学号是否已存在
        User existUser = userMapper.findByStudentId(user.getStudentId());
        if (existUser != null) {
            return false;
        }

        // 2. 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 3. 设置默认值
        user.setCreditScore(100);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 4. 插入数据库
        return userMapper.insert(user) > 0;
    }

    //登录
    @Override
    public User login(String studentId, String password) {
        // 根据学号查询用户
        User user = userMapper.findByStudentId(studentId);

        // 用户存在且密码匹配
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    // 获取个人信息
    @Override
    public User getUserInfo(Long userId) {
        return userMapper.findByUserId(userId);
    }

    // 查看我的发布
    // 注意：这个方法需要 item 表的 Mapper，目前返回空列表，等 item 模块完成后实现
    @Override
    public List<Map<String, Object>> getMyItems(Long userId) {
        // 临时方案：直接用 UserMapper 查 item 表
        // TODO: 等 module-m2-item 完成后，改为 return itemMapper.selectByUserId(userId);
        return userMapper.selectUserItems(userId);
    }

    // 查看我的订单
    // 注意：这个方法需要 order 表的 Mapper，目前返回空列表，等 order 模块完成后实现
    @Override
    public List<Map<String, Object>> getMyOrders(Long userId, String type) {
        // 临时方案：直接用 UserMapper 查 orders 表
        // TODO: 等 module-m4-order 完成后，改为调用 OrderMapper
        if ("buy".equals(type)) {
            return userMapper.selectBuyerOrders(userId);
        } else {
            return userMapper.selectSellerOrders(userId);
        }
    }


    /**
     * 修改个人资料
     */
    @Override
    public boolean updateUserInfo(Long userId, String username, String phone, String avatar) {
        // 先检查用户是否存在
        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return false;
        }

        // 更新字段（只更新非空字段）
        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }
        if (phone != null && !phone.isEmpty()) {
            user.setPhone(phone);
        }
        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatar(avatar);
        }
        user.setUpdateTime(LocalDateTime.now());

        return userMapper.update(user) > 0;
    }

    /**
     * 修改密码
     */
    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 查询用户
        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return false;
        }

        // 2. 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        // 3. 加密新密码
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        // 4. 更新密码
        return userMapper.updatePassword(encodedNewPassword, LocalDateTime.now(), userId) > 0;
    }
}