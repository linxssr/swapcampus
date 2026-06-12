package com.campus.admin.controller;

import com.campus.admin.dto.*;
import com.campus.admin.service.AdminService;
import com.campus.common.utils.JwtUtil;
import com.campus.model.entity.Admin;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    public AdminController(AdminService adminService, JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 1. 商品审核
     * PUT /api/v1/admin/item/audit
     */
    @PutMapping("/item/audit")
    public Map<String, Object> auditItem(@RequestHeader("Authorization") String token,
                                         @RequestBody AuditRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 验证管理员 token
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        // 参数校验
        if (request.getItemId() == null || request.getAuditStatus() == null) {
            result.put("code", 400);
            result.put("message", "商品ID和审核状态不能为空");
            return result;
        }
        if (request.getAuditStatus() != 1 && request.getAuditStatus() != 2) {
            result.put("code", 400);
            result.put("message", "审核状态只能是1(通过)或2(驳回)");
            return result;
        }

        Long adminId = jwtUtil.getUserIdFromToken(token);
        boolean success = adminService.auditItem(request.getItemId(), request.getAuditStatus(), adminId);

        if (success) {
            result.put("code", 200);
            result.put("message", "审核成功");
        } else {
            result.put("code", 500);
            result.put("message", "审核失败");
        }
        return result;
    }

    /**
     * 2. 用户封禁/解封
     * PUT /api/v1/admin/user/ban/{uid}
     */
    @PutMapping("/user/ban/{uid}")
    public Map<String, Object> banUser(@RequestHeader("Authorization") String token,
                                       @PathVariable Long uid,
                                       @RequestParam Integer status) {
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        if (status == null || (status != 0 && status != 1)) {
            result.put("code", 400);
            result.put("message", "状态只能是0(封禁)或1(解封)");
            return result;
        }

        boolean success = adminService.banUser(uid, status);
        String statusText = status == 0 ? "封禁" : "解封";

        if (success) {
            result.put("code", 200);
            result.put("message", "用户" + statusText + "成功");
        } else {
            result.put("code", 500);
            result.put("message", "用户" + statusText + "失败");
        }
        return result;
    }

    /**
     * 3. 全部举报列表
     * GET /api/v1/admin/report/list?handleStatus=0/1
     */
    @GetMapping("/report/list")
    public Map<String, Object> getReportList(@RequestHeader("Authorization") String token,
                                             @RequestParam(required = false) Integer handleStatus) {
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        List<Map<String, Object>> list = adminService.getReportList(handleStatus);
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", list);
        return result;
    }

    /**
     * 4. 处理举报
     * PUT /api/v1/admin/report/handle
     */
    @PutMapping("/report/handle")
    public Map<String, Object> handleReport(@RequestHeader("Authorization") String token,
                                            @RequestBody HandleReportRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        if (request.getReportId() == null || request.getResult() == null || request.getResult().isEmpty()) {
            result.put("code", 400);
            result.put("message", "举报ID和处理结果不能为空");
            return result;
        }

        Long adminId = jwtUtil.getUserIdFromToken(token);
        boolean success = adminService.handleReport(request.getReportId(), adminId, request.getResult());

        if (success) {
            result.put("code", 200);
            result.put("message", "处理成功");
        } else {
            result.put("code", 500);
            result.put("message", "处理失败");
        }
        return result;
    }

    /**
     * 5. 新增分类
     * POST /api/v1/admin/category/add
     */
    @PostMapping("/category/add")
    public Map<String, Object> addCategory(@RequestHeader("Authorization") String token,
                                           @RequestBody CategoryAddRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        if (request.getName() == null || request.getName().isEmpty()) {
            result.put("code", 400);
            result.put("message", "分类名称不能为空");
            return result;
        }

        Integer sort = request.getSort() != null ? request.getSort() : 0;
        boolean success = adminService.addCategory(request.getName(), sort);

        if (success) {
            result.put("code", 200);
            result.put("message", "新增成功");
        } else {
            result.put("code", 500);
            result.put("message", "新增失败");
        }
        return result;
    }

    /**
     * 6. 修改分类
     * PUT /api/v1/admin/category/update
     */
    @PutMapping("/category/update")
    public Map<String, Object> updateCategory(@RequestHeader("Authorization") String token,
                                              @RequestBody CategoryUpdateRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        if (request.getCategoryId() == null) {
            result.put("code", 400);
            result.put("message", "分类ID不能为空");
            return result;
        }
        if (request.getName() == null || request.getName().isEmpty()) {
            result.put("code", 400);
            result.put("message", "分类名称不能为空");
            return result;
        }

        Integer sort = request.getSort() != null ? request.getSort() : 0;
        boolean success = adminService.updateCategory(request.getCategoryId(), request.getName(), sort);

        if (success) {
            result.put("code", 200);
            result.put("message", "修改成功");
        } else {
            result.put("code", 500);
            result.put("message", "修改失败");
        }
        return result;
    }

    /**
     * 管理员登录
     * POST /api/v1/admin/login
     */
    @PostMapping("/login")
    public Map<String, Object> adminLogin(@RequestBody AdminLoginRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (request.getAdminName() == null || request.getAdminName().isEmpty()) {
            result.put("code", 400);
            result.put("message", "管理员账号不能为空");
            return result;
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            result.put("code", 400);
            result.put("message", "密码不能为空");
            return result;
        }

        Admin admin = adminService.adminLogin(request.getAdminName(), request.getPassword());

        if (admin != null) {
            String token = jwtUtil.generateToken(admin.getAdminId(), admin.getAdminName());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("adminId", admin.getAdminId());
            data.put("adminName", admin.getAdminName());
            data.put("realName", admin.getRealName());

            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", data);
        } else {
            result.put("code", 401);
            result.put("message", "账号或密码错误");
        }
        return result;
    }

    /**
     * 添加管理员
     * POST /api/v1/admin/add
     */
    @PostMapping("/add")
    public Map<String, Object> addAdmin(@RequestBody AddAdminRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (request.getAdminName() == null || request.getAdminName().isEmpty()) {
            result.put("code", 400);
            result.put("message", "管理员账号不能为空");
            return result;
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            result.put("code", 400);
            result.put("message", "密码不能为空");
            return result;
        }

        boolean success = adminService.addAdmin(
                request.getAdminName(),
                request.getPassword(),
                request.getRealName(),
                request.getPhone()
        );

        if (success) {
            result.put("code", 200);
            result.put("message", "添加成功");
        } else {
            result.put("code", 409);
            result.put("message", "管理员账号已存在");
        }
        return result;
    }
}