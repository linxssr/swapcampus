package com.campus.admin.service;

import java.util.List;
import java.util.Map;
import com.campus.model.entity.Admin;

public interface AdminService {

    // 商品审核
    boolean auditItem(Long itemId, Integer auditStatus, Long adminId);

    // 用户封禁/解封
    boolean banUser(Long userId, Integer status);

    // 获取举报列表
    List<Map<String, Object>> getReportList(Integer handleStatus, String keyword, int offset, int size);
    int countReportList(Integer handleStatus, String keyword);

    // 处理举报
    boolean handleReport(Long reportId, Long adminId, String result);

    // 新增分类
    boolean addCategory(String name, Integer sort);

    // 修改分类
    boolean updateCategory(Long categoryId, String name, Integer sort);

    // 管理员登录（新增）
    Admin adminLogin(String adminName, String password);

    // 添加管理员
    boolean addAdmin(String adminName, String password, String realName, String phone);

    // 获取商品列表（分页）
    List<Map<String, Object>> getItemList(Integer auditStatus, String keyword, int offset, int size);

    int countItemList(Integer auditStatus, String keyword);

    // 统计商品总数

    // 分类列表
    List<Map<String, Object>> getCategoryList(String keyword, int offset, int size);
    int countCategoryList(String keyword);

    // 修改分类状态
    boolean updateCategoryStatus(Long categoryId, Integer status);

    // 仪表盘统计数据
    Map<String, Object> getDashboardStat();

    // 近7天趋势数据
    Map<String, Object> getTrendData();

    // 订单状态分布
    List<Map<String, Object>> getOrderStatusStat();

    // 分类商品统计
    List<Map<String, Object>> getCategoryStats();

    // 调整用户信用分
    int updateCreditScore(Long userId, Integer changeAmount);

    // 订单列表
    List<Map<String, Object>> getOrderList(Integer orderStatus, String keyword, int offset, int size);
    int countOrderList(Integer orderStatus, String keyword);
    Map<String, Integer> getOrderStatusCount();
}