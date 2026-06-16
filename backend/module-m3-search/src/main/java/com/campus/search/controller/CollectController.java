package com.campus.search.controller;

import com.campus.common.interceptor.AuthAnnotation;
import com.campus.common.result.Result;
import com.campus.common.utils.JwtUtil;
import com.campus.model.vo.CollectVO;
import com.campus.search.service.SearchService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/collect")
public class CollectController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/add")
    @AuthAnnotation
    public Result<Void> addCollect(@RequestBody Map<String, Long> requestBody, HttpServletRequest request) {
        Long itemId = requestBody.get("itemId");
        Long userId = resolveUserId(request);
        searchService.addCollect(userId, itemId);
        return Result.success();
    }

    @DeleteMapping("/cancel/{id}")
    @AuthAnnotation
    public Result<Void> cancelCollect(@PathVariable Long id, HttpServletRequest request) {
        Long userId = resolveUserId(request);
        searchService.cancelCollect(id, userId);
        return Result.success();
    }

    @GetMapping("/my")
    @AuthAnnotation
    public Result<List<CollectVO>> listMyCollects(HttpServletRequest request) {
        Long userId = resolveUserId(request);
        List<CollectVO> collects = searchService.listMyCollects(userId);
        return Result.success(collects);
    }

    private Long resolveUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return Long.valueOf(String.valueOf(userId));
        }

        Long parsedUserId = jwtUtil.parseUserId(request.getHeader("Authorization"));
        if (parsedUserId != null) {
            return parsedUserId;
        }

        parsedUserId = jwtUtil.parseUserId(request.getHeader("token"));
        if (parsedUserId != null) {
            return parsedUserId;
        }

        return 1L;
    }
}
