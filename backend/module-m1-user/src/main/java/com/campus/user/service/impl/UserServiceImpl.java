package com.campus.user.service.impl;

import com.campus.mapper.OrderMapper;
import com.campus.mapper.UserMapper;
import com.campus.model.entity.User;
import com.campus.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Autowired
    private OrderMapper orderMapper;


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

    @Override
    public List<Map<String, Object>> getMyOrders(Long userId, String type) {
        if ("buy".equals(type)) {
            return orderMapper.selectBuyerOrders(userId);
        } else {
            return orderMapper.selectSellerOrders(userId);
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

    /**
     * 批量获取订单的商品名称
     */
    @Override
    public List<Map<String, Object>> getOrderItems(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userMapper.getOrderItems(orderIds);
    }

    /**
     * 批量获取商品信息
     * @param itemIds 商品ID列表
     * @return 商品列表
     */
    @Override
    public List<Map<String, Object>> getItemsByIds(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userMapper.getItemsByIds(itemIds);
    }

    /**
     * 上传头像
     * @param userId 用户ID
     * @param file 图片文件
     * @return 头像URL
     */
    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        try {
            // 生成文件名：userId_时间戳.扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = userId + "_" + System.currentTimeMillis() + extension;

            // 保存到本地目录（或MinIO）
            // 方案1：保存到本地（简单，但部署后可能丢失）
            String uploadDir = "uploads/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File destFile = new File(uploadDir + filename);
            file.transferTo(destFile);

            // 返回访问URL（需要配置静态资源映射）
            String avatarUrl = "/uploads/avatars/" + filename;

            // 方案2：保存到MinIO（推荐）
            // String avatarUrl = minioUtil.uploadFile(file, "avatars/" + filename);

            // 更新用户表中的avatar字段
            userMapper.updateAvatar(userId, avatarUrl);

            return avatarUrl;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }
    @Override
    public List<Map<String, Object>> getUserList(String keyword, Integer status, int offset, int size) {
        return userMapper.selectUserList(keyword, status, offset, size);
    }

    @Override
    public int countUserList(String keyword, Integer status) {
        return userMapper.countUserList(keyword, status);
    }

    @Override
    public Map<String, Object> sign(Long userId) {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();

        // 检查今天是否已签到
        int existCount = userMapper.checkSignToday(userId, today);
        if (existCount > 0) {
            throw new RuntimeException("今日已签到");
        }

        // 获取上次签到日期
        LocalDate lastSignDate = userMapper.getLastSignDate(userId);

        // 计算连续签到天数
        int continuousDays = 1;
        if (lastSignDate != null && lastSignDate.equals(today.minusDays(1))) {
            // 昨天签到了，连续天数+1
            Integer lastContinuous = userMapper.getLastContinuousDays(userId);
            continuousDays = (lastContinuous != null ? lastContinuous : 0) + 1;
        }

        // 计算增加分数
        int addScore = 1;
        if (continuousDays == 7) {
            addScore = 5;  // 连续7天额外奖励
        }

        // 插入签到记录
        userMapper.insertSignRecord(userId, today, continuousDays);

        // 更新用户信用分
        userMapper.updateCreditScore(userId, addScore);

        // 获取最新信用分
        Integer newScore = userMapper.getCreditScore(userId);

        result.put("addScore", addScore);
        result.put("newScore", newScore);
        result.put("continuousDays", continuousDays);

        return result;
    }

    @Override
    public Map<String, Object> getSignStatus(Long userId) {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();

        // 今天是否已签到
        boolean signedToday = userMapper.checkSignToday(userId, today) > 0;

        // 连续签到天数
        Integer continuousDays = userMapper.getLastContinuousDays(userId);
        if (continuousDays == null) continuousDays = 0;

        result.put("signedToday", signedToday);
        result.put("continuousDays", continuousDays);

        return result;
    }

    @Override
    public String getRealNameByStudentId(String studentId) {
        return userMapper.getRealNameByStudentId(studentId);
    }

    @Override
    public boolean checkStudentIdExists(String studentId) {
        int count = userMapper.checkStudentIdExists(studentId);
        return count > 0;  // 将 int 转换为 boolean
    }
}