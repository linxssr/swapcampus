package com.campus.user.controller;

import com.campus.common.utils.JwtUtil;
import com.campus.model.entity.User;
import com.campus.user.dto.*;
import com.campus.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/user")
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
}