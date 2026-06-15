package com.campus.common.interceptor;

import com.campus.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    public static final String HEADER_AUTH = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ATTR_USER_ID = "userId";

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long userId = jwtUtil.parseUserId(request.getHeader(HEADER_AUTH));
        if (userId == null) {
            userId = jwtUtil.parseUserId(request.getHeader("token"));
        }
        if (userId != null) {
            request.setAttribute(ATTR_USER_ID, userId);
        }
        return true;
    }
}
