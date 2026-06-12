package com.campus.admin.service.impl;

import com.campus.mapper.*;
import com.campus.model.entity.Admin;
import com.campus.admin.service.AdminService;
import com.campus.model.entity.Category;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final ItemMapper itemMapper;
    private final AdminMapper adminMapper;
    private final ReportMapper reportMapper;
    private final CategoryMapper categoryMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminServiceImpl(ItemMapper itemMapper, AdminMapper adminMapper,
                            ReportMapper reportMapper, CategoryMapper categoryMapper) {
        this.itemMapper = itemMapper;
        this.adminMapper = adminMapper;
        this.reportMapper = reportMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public boolean auditItem(Long itemId, Integer auditStatus, Long adminId) {
        return adminMapper.auditItem(itemId, auditStatus, adminId, LocalDateTime.now()) > 0;
    }

    @Override
    public boolean banUser(Long userId, Integer status) {
        return adminMapper.updateUserStatus(userId, status) > 0;
    }

    @Override
    public List<Map<String, Object>> getReportList(Integer handleStatus) {
        return reportMapper.selectReportList(handleStatus);
    }

    @Override
    public boolean handleReport(Long reportId, Long adminId, String result) {
        return reportMapper.handleReport(reportId, adminId, result, LocalDateTime.now()) > 0;
    }

    @Override
    public boolean addCategory(String name, Integer sort) {
        // 检查分类名是否已存在（可选）
        Category category = new Category();
        category.setCategoryName(name);
        category.setSort(sort != null ? sort : 0);
        return categoryMapper.insertCategory(category) > 0;
    }

    @Override
    public boolean updateCategory(Long categoryId, String name, Integer sort) {
        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setCategoryName(name);
        category.setSort(sort != null ? sort : 0);
        return categoryMapper.updateCategory(category) > 0;
    }

    // 管理员登录（新增）
    @Override
    public Admin adminLogin(String adminName, String password) {
        Admin admin = adminMapper.findByAdminName(adminName);
        if (admin != null && passwordEncoder.matches(password, admin.getPassword())) {
            return admin;
        }
        return null;
    }

    //添加管理员
    @Override
    public boolean addAdmin(String adminName, String password, String realName, String phone) {
        // 检查用户名是否已存在
        Admin existAdmin = adminMapper.findByAdminName(adminName);
        if (existAdmin != null) {
            return false;
        }

        Admin admin = new Admin();
        admin.setAdminName(adminName);
        admin.setPassword(passwordEncoder.encode(password));  // 加密
        admin.setRealName(realName);
        admin.setPhone(phone);

        return adminMapper.insertAdmin(admin) > 0;
    }
}