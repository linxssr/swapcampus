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
     * 举报列表（分页 + 筛选 + 搜索）
     * GET /api/v1/admin/report/list?handleStatus=0&page=1&size=10&keyword=xxx
     */
    @GetMapping("/report/list")
    public Map<String, Object> getReportList(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Integer handleStatus,
            @RequestParam(defaultValue = "1") Integer page,           // 新增
            @RequestParam(defaultValue = "10") Integer size,         // 新增
            @RequestParam(required = false) String keyword) {        // 新增
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        try {
            int offset = (page - 1) * size;
            List<Map<String, Object>> list = adminService.getReportList(handleStatus, keyword, offset, size);
            int total = adminService.countReportList(handleStatus, keyword);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", list);
            result.put("page", page);
            result.put("size", size);
            result.put("total", total);
            result.put("totalPages", (int) Math.ceil((double) total / size));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

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

    /**
     * 获取商品列表（分页 + 按审核状态筛选）
     * GET /api/v1/admin/item/list?auditStatus=0&page=1&size=10
     * @param auditStatus 审核状态: 0-待审核 1-已通过 2-已驳回，不传则查全部
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     */
    @GetMapping("/item/list")
    public Map<String, Object> getItemList(
            @RequestParam(value = "auditStatus", required = false) Integer auditStatus,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        // 验证管理员token
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        try {
            // 计算偏移量
            int offset = (page - 1) * size;

            // 查询数据
            List<Map<String, Object>> items = adminService.getItemList(auditStatus, keyword, offset, size);
            int total = adminService.countItemList(auditStatus, keyword); ;

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", items);
            result.put("page", page);
            result.put("size", size);
            result.put("total", total);
            result.put("totalPages", (int) Math.ceil((double) total / size));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 分类列表（分页 + 搜索）
     * GET /api/v1/admin/category/list?page=1&size=10&keyword=
     */
    @GetMapping("/category/list")
    public Map<String, Object> getCategoryList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        // 验证管理员token
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        try {
            int offset = (page - 1) * size;
            List<Map<String, Object>> list = adminService.getCategoryList(keyword, offset, size);
            int total = adminService.countCategoryList(keyword);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", list);
            result.put("page", page);
            result.put("size", size);
            result.put("total", total);
            result.put("totalPages", (int) Math.ceil((double) total / size));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 修改分类状态（启用/禁用）
     * PUT /api/v1/admin/category/status?categoryId=1&status=0
     * status: 0-禁用 1-启用
     */
    @PutMapping("/category/status")
    public Map<String, Object> updateCategoryStatus(
            @RequestParam Long categoryId,
            @RequestParam Integer status,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        // 验证管理员token
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        try {
            boolean success = adminService.updateCategoryStatus(categoryId, status);
            if (success) {
                result.put("code", 200);
                result.put("message", status == 1 ? "分类已启用" : "分类已禁用");
            } else {
                result.put("code", 500);
                result.put("message", "操作失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * 仪表盘统计数据
     * GET /api/v1/admin/dashboard/stat
     */
    @GetMapping("/dashboard/stat")
    public Map<String, Object> getDashboardStat(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        try {
            Map<String, Object> data = adminService.getDashboardStat();
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 近7天数据统计（用于图表）
     * GET /api/v1/admin/dashboard/trend
     */
    @GetMapping("/dashboard/trend")
    public Map<String, Object> getTrendData(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        try {
            Map<String, Object> data = adminService.getTrendData();
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 订单状态分布统计
     * GET /api/v1/admin/dashboard/order-status
     */
    @GetMapping("/dashboard/order-status")
    public Map<String, Object> getOrderStatusStat(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        try {
            List<Map<String, Object>> data = adminService.getOrderStatusStat();
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 分类商品数量统计（Top 5）
     * GET /api/v1/admin/dashboard/category-stats
     */
    @GetMapping("/dashboard/category-stats")
    public Map<String, Object> getCategoryStats(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        try {
            List<Map<String, Object>> data = adminService.getCategoryStats();
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }
    /**
     * 调整用户信用分
     * PUT /api/v1/admin/user/credit
     * Body: {"userId": 69, "changeAmount": 5}  正数加分，负数减分
     */
    @PutMapping("/user/credit")
    public Map<String, Object> updateCreditScore(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        // 验证管理员token
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        try {
            Long userId = ((Number) request.get("userId")).longValue();
            Integer changeAmount = (Integer) request.get("changeAmount");

            if (changeAmount == null) {
                result.put("code", 400);
                result.put("message", "请填写调整分数");
                return result;
            }

            int newScore = adminService.updateCreditScore(userId, changeAmount);

            result.put("code", 200);
            result.put("message", changeAmount > 0 ? "信用分增加成功" : "信用分减少成功");
            result.put("data", newScore);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }

        return result;
    }

    /**
     * 订单列表（分页 + 筛选 + 搜索）
     * GET /api/v1/admin/order/list?page=1&size=10&orderStatus=1&keyword=
     */
    @GetMapping("/order/list")
    public Map<String, Object> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) String keyword,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        try {
            int offset = (page - 1) * size;
            List<Map<String, Object>> orders = adminService.getOrderList(orderStatus, keyword, offset, size);
            int total = adminService.countOrderList(orderStatus, keyword);

            // 获取各状态订单数量
            Map<String, Integer> statusCount = adminService.getOrderStatusCount();

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", orders);
            result.put("page", page);
            result.put("size", size);
            result.put("total", total);
            result.put("totalPages", (int) Math.ceil((double) total / size));
            result.put("statusCount", statusCount);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }
}