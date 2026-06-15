package com.campus.user.controller;

import com.campus.common.utils.JwtUtil;
import com.campus.model.entity.User;
import com.campus.user.dto.*;
import com.campus.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 构造方法注入
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户注册接口
     * POST /api/v1/user/register
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 参数校验
        if (request.getStudentId() == null || request.getStudentId().isEmpty()) {
            result.put("code", 400);
            result.put("message", "学号不能为空");
            return result;
        }
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            result.put("code", 400);
            result.put("message", "昵称不能为空");
            return result;
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            result.put("code", 400);
            result.put("message", "密码不能为空");
            return result;
        }


        // 组装 User 对象
        User user = new User();
        user.setStudentId(request.getStudentId());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());

        // 调用注册服务
        boolean success = userService.register(user);

        if (success) {
            result.put("code", 200);
            result.put("message", "注册成功");
        } else {
            result.put("code", 409);
            result.put("message", "学号已存在");
        }
        return result;
    }

    /**
     * 用户登录接口
     * POST /api/v1/user/login
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 参数校验
        if (request.getStudentId() == null || request.getStudentId().isEmpty()) {
            result.put("code", 400);
            result.put("message", "学号不能为空");
            return result;
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            result.put("code", 400);
            result.put("message", "密码不能为空");
            return result;
        }

        // 调用登录服务
        User user = userService.login(request.getStudentId(), request.getPassword());

        if (user != null) {
            // 生成 JWT token
            String token = jwtUtil.generateToken(user.getUserId(), user.getStudentId());

            // 返回登录响应
            LoginResponse response = new LoginResponse(
                    token,
                    user.getUserId(),
                    user.getUsername(),
                    user.getCreditScore()
            );

            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", response);
        } else {
            result.put("code", 401);
            result.put("message", "学号或密码错误");
        }
        return result;
    }
    /**
     * 3. 获取个人信息
     * GET /api/v1/user/info
     * 需要 Header: Authorization: Bearer <token>
     */
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        // 验证 token
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        // 从 token 获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserInfo(userId);

        if (user == null) {
            result.put("code", 404);
            result.put("message", "用户不存在");
            return result;
        }

        // 格式化时间
        String createTime = user.getCreateTime() != null ?
                user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;

        UserInfoResponse response = new UserInfoResponse(
                user.getUserId(),
                user.getStudentId(),
                user.getUsername(),
                user.getPhone(),
                user.getAvatar(),
                user.getCreditScore(),
                user.getStatus(),
                createTime
        );

        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", response);
        return result;
    }

    /**
     * 4. 查看我的发布
     * GET /api/v1/user/myItem
     * 需要 Header: Authorization: Bearer <token>
     */
    @GetMapping("/myItem")
    public Map<String, Object> getMyItems(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        // 验证 token
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Map<String, Object>> items = userService.getMyItems(userId);

        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", items);
        return result;
    }

    /**
     * 5. 查看我的订单
     * GET /api/v1/user/myOrder?type=buy 或 type=sell
     * 需要 Header: Authorization: Bearer <token>
     */
    @GetMapping("/myOrder")
    public Map<String, Object> getMyOrders(
            @RequestHeader("Authorization") String token,
            @RequestParam("type") String type) {
        Map<String, Object> result = new HashMap<>();

        // 验证 token
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        // 参数校验
        if (!"buy".equals(type) && !"sell".equals(type)) {
            result.put("code", 400);
            result.put("message", "type参数必须是 buy 或 sell");
            return result;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Map<String, Object>> orders = userService.getMyOrders(userId, type);

        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", orders);
        return result;
    }

    /**
     * 修改个人资料
     * PUT /api/v1/user/update
     */
    @PutMapping("/update")
    public Map<String, Object> updateUserInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody UserUpdateRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 验证 token
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        // 至少有一个字段需要更新
        if ((request.getUsername() == null || request.getUsername().isEmpty()) &&
                (request.getPhone() == null || request.getPhone().isEmpty()) &&
                (request.getAvatar() == null || request.getAvatar().isEmpty())) {
            result.put("code", 400);
            result.put("message", "至少需要修改一个字段");
            return result;
        }

        boolean success = userService.updateUserInfo(
                userId,
                request.getUsername(),
                request.getPhone(),
                request.getAvatar()
        );

        if (success) {
            result.put("code", 200);
            result.put("message", "修改成功");
        } else {
            result.put("code", 404);
            result.put("message", "用户不存在");
        }
        return result;
    }

    /**
     * 修改密码
     * PUT /api/v1/user/pwd
     */
    @PutMapping("/pwd")
    public Map<String, Object> updatePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody PasswordUpdateRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 验证 token
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        // 参数校验
        if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
            result.put("code", 400);
            result.put("message", "旧密码不能为空");
            return result;
        }
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            result.put("code", 400);
            result.put("message", "新密码不能为空");
            return result;
        }
        if (request.getNewPassword().length() < 6) {
            result.put("code", 400);
            result.put("message", "新密码长度不能少于6位");
            return result;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        boolean success = userService.updatePassword(
                userId,
                request.getOldPassword(),
                request.getNewPassword()
        );

        if (success) {
            result.put("code", 200);
            result.put("message", "密码修改成功，请重新登录");
        } else {
            result.put("code", 401);
            result.put("message", "旧密码错误");
        }
        return result;
    }

    /**
     * 批量获取订单的商品名称
     * GET /api/v1/user/order/item-titles
     * 需要 Header: Authorization: Bearer <token>
     */
    @GetMapping("/order/item-titles")
    public Map<String, Object> getOrderItemTitles(@RequestParam("orderIds") String orderIds) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 解析订单ID，例如 "1,2,3" -> [1,2,3]
            List<Long> orderIdList = Arrays.stream(orderIds.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            // 查询订单对应的商品ID和商品名称
            List<Map<String, Object>> items = userService.getOrderItems(orderIdList);

            // 转换为 Map<orderId, itemTitle>
            Map<Long, String> titleMap = items.stream()
                    .collect(Collectors.toMap(
                            m -> (Long) m.get("order_id"),
                            m -> (String) m.get("item_title")
                    ));

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", titleMap);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 批量获取商品信息
     * GET /api/v1/user/items/batch?ids=1,2,3
     */
    @GetMapping("/items/batch")
    public Map<String, Object> getItemsByIds(@RequestParam("ids") String ids) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Long> itemIds = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            List<Map<String, Object>> items = userService.getItemsByIds(itemIds);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", items);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 上传头像
     * POST /api/v1/user/avatar
     * 需要 Header: Authorization: Bearer <token>
     *
     * @param file 上传的图片文件
     * @param token 用户token
     * @return 图片URL
     */
    @PostMapping("/avatar")
    public Map<String, Object> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证token，获取用户ID
            if (!jwtUtil.validateToken(token)) {
                result.put("code", 401);
                result.put("message", "Token无效或已过期");
                return result;
            }

            Long userId = jwtUtil.getUserIdFromToken(token);

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("code", 400);
                result.put("message", "只能上传图片文件");
                return result;
            }

            // 验证文件大小（限制2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                result.put("code", 400);
                result.put("message", "图片大小不能超过2MB");
                return result;
            }

            // 上传图片并获取URL
            String avatarUrl = userService.uploadAvatar(userId, file);

            result.put("code", 200);
            result.put("message", "上传成功");
            result.put("data", avatarUrl);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 获取用户列表（分页 + 搜索）- 管理员专用
     * GET /api/v1/user/list?page=1&size=10&keyword=
     */
    @GetMapping("/list")
    public Map<String, Object> getUserList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        // 验证管理员token
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        // 验证是否是管理员（可选：从token中获取角色）
        Long adminId = jwtUtil.getUserIdFromToken(token);
        if (adminId == null) {
            result.put("code", 403);
            result.put("message", "无权限访问");
            return result;
        }

        try {
            int offset = (page - 1) * size;

            List<Map<String, Object>> users = userService.getUserList(keyword, status, offset, size);
            int total = userService.countUserList(keyword, status);

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", users);
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
     * 用户签到
     * POST /api/v1/user/sign
     */
    @PostMapping("/sign")
    public Map<String, Object> sign(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        try {
            Map<String, Object> signResult = userService.sign(userId);
            result.put("code", 200);
            result.put("message", "签到成功");
            result.put("data", signResult);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 查询签到状态
     * GET /api/v1/user/sign/status
     */
    @GetMapping("/sign/status")
    public Map<String, Object> getSignStatus(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        try {
            Map<String, Object> status = userService.getSignStatus(userId);
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", status);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 根据学号获取真实姓名
     * GET /api/v1/user/real-name/{studentId}
     */
    @GetMapping("/real-name/{studentId}")
    public Map<String, Object> getRealNameByStudentId(@PathVariable String studentId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String realName = userService.getRealNameByStudentId(studentId);
            if (realName != null) {
                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", realName);
            } else {
                result.put("code", 404);
                result.put("message", "未找到该学号对应的真实姓名");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    /**
     * 获取用户公开信息（用于聊天等场景）
     * GET /api/v1/user/profile/{userId}
     */
    @GetMapping("/profile/{userId}")
    public Map<String, Object> getUserProfile(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            User user = userService.getUserInfo(userId);
            if (user != null) {
                Map<String, Object> profile = new HashMap<>();
                profile.put("userId", user.getUserId());
                profile.put("username", user.getUsername());
                profile.put("creditScore", user.getCreditScore());
                profile.put("avatar", user.getAvatar());

                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", profile);
            } else {
                result.put("code", 404);
                result.put("message", "用户不存在");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            result.put("data", null);
        }

        return result;
    }


}