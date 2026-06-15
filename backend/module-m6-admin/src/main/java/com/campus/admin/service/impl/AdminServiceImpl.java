package com.campus.admin.service.impl;

import com.campus.mapper.*;
import com.campus.model.entity.Admin;
import com.campus.admin.service.AdminService;
import com.campus.model.entity.Category;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
    public List<Map<String, Object>> getReportList(Integer handleStatus, String keyword, int offset, int size) {
        return adminMapper.selectReportList(handleStatus, keyword, offset, size);
    }

    @Override
    public int countReportList(Integer handleStatus, String keyword) {
        return adminMapper.countReportList(handleStatus, keyword);
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

    @Override
    public List<Map<String, Object>> getItemList(Integer auditStatus, String keyword, int offset, int size) {
        return adminMapper.selectItemList(auditStatus, keyword, offset, size);
    }

    @Override
    public int countItemList(Integer auditStatus, String keyword) {
        return adminMapper.countItemList(auditStatus, keyword);
    }

    @Override
    public List<Map<String, Object>> getCategoryList(String keyword, int offset, int size) {
        return adminMapper.selectCategoryList(keyword, offset, size);
    }

    @Override
    public int countCategoryList(String keyword) {
        return adminMapper.countCategoryList(keyword);
    }

    @Override
    public boolean updateCategoryStatus(Long categoryId, Integer status) {
        return adminMapper.updateCategoryStatus(categoryId, status) > 0;
    }

    @Override
    public Map<String, Object> getDashboardStat() {
        Map<String, Object> stat = new HashMap<>();

        // 待审核商品数量
        int pendingItem = adminMapper.countPendingItems();
        // 待处理举报数量
        int pendingReport = adminMapper.countPendingReports();
        // 总用户数
        int totalUser = adminMapper.countTotalUsers();
        // 总商品数
        int totalItem = adminMapper.countTotalItems();
        // 今日新增用户
        int todayNewUser = adminMapper.countTodayNewUsers();
        // 今日新增商品
        int todayNewItem = adminMapper.countTodayNewItems();
        // 今日订单数
        int todayOrderCount = adminMapper.countTodayOrders();
        // 今日成交额
        BigDecimal todayIncome = adminMapper.getTodayIncome();

        stat.put("pendingItem", pendingItem);
        stat.put("pendingReport", pendingReport);
        stat.put("totalUser", totalUser);
        stat.put("totalItem", totalItem);
        stat.put("todayNewUser", todayNewUser);
        stat.put("todayNewItem", todayNewItem);
        stat.put("todayOrderCount", todayOrderCount);
        stat.put("todayIncome", todayIncome != null ? todayIncome : BigDecimal.ZERO);

        return stat;
    }

    @Override
    public Map<String, Object> getTrendData() {
        Map<String, Object> result = new HashMap<>();

        List<String> dates = new ArrayList<>();
        List<Integer> newUsers = new ArrayList<>();
        List<Integer> newItems = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            dates.add(date);
            newUsers.add(adminMapper.countNewUsersByDate(date));
            newItems.add(adminMapper.countNewItemsByDate(date));
        }

        result.put("dates", dates);
        result.put("newUsers", newUsers);
        result.put("newItems", newItems);

        return result;
    }

    @Override
    public List<Map<String, Object>> getOrderStatusStat() {
        return adminMapper.getOrderStatusStat();
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        return adminMapper.getCategoryStats();
    }

    @Override
    public int updateCreditScore(Long userId, Integer changeAmount) {
        // 获取当前信用分
        Integer currentScore = adminMapper.getCreditScore(userId);
        if (currentScore == null) {
            throw new RuntimeException("用户不存在");
        }

        int newScore = currentScore + changeAmount;
        // 限制范围 0-200
        if (newScore < 0) newScore = 0;
        if (newScore > 200) newScore = 200;

        adminMapper.updateCreditScore(userId, newScore);

        return newScore;
    }

    @Override
    public List<Map<String, Object>> getOrderList(Integer orderStatus, String keyword, int offset, int size) {
        return adminMapper.selectOrderList(orderStatus, keyword, offset, size);
    }

    @Override
    public int countOrderList(Integer orderStatus, String keyword) {
        return adminMapper.countOrderList(orderStatus, keyword);
    }

    @Override
    public Map<String, Integer> getOrderStatusCount() {
        Map<String, Integer> countMap = new HashMap<>();
        countMap.put("pending", adminMapper.countOrdersByStatus(1));    // 待确认
        countMap.put("waiting", adminMapper.countOrdersByStatus(2));    // 待取件
        countMap.put("completed", adminMapper.countOrdersByStatus(3));  // 已完成
        countMap.put("canceled", adminMapper.countOrdersByStatus(4));   // 已取消
        return countMap;
    }
}