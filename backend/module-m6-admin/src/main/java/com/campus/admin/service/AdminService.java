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
    List<Map<String, Object>> getReportList(Integer handleStatus);

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
}