package com.campus.order.controller;

import com.campus.common.result.Result;
import com.campus.common.utils.JwtUtil;
import com.campus.model.entity.Comment;
import com.campus.order.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/add")
    public Result<Void> addComment(@RequestBody Map<String, Object> requestData,
                                    HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "请先登录");
        }

        Long buyerId = jwtUtil.getUserIdFromToken(token);
        Long orderId = Long.valueOf(requestData.get("orderId").toString());
        Integer score = Integer.valueOf(requestData.get("score").toString());
        String content = String.valueOf(requestData.get("content"));

        commentService.addComment(buyerId, orderId, score, content);
        return Result.success(null, "评价成功");
    }

    @GetMapping("/item/{itemId}")
    public Result<List<Map<String, Object>>> getCommentsByItemId(@PathVariable("itemId") Long itemId) {
        List<Map<String, Object>> comments = commentService.getCommentsByItemId(itemId);
        return Result.success(comments);
    }

    @GetMapping("/order/{orderId}")
    public Result<Comment> getCommentByOrderId(@PathVariable("orderId") Long orderId) {
        Comment comment = commentService.getCommentByOrderId(orderId);
        return Result.success(comment);
    }

    @GetMapping("/seller/{sellerId}")
    public Result<List<Map<String, Object>>> getSellerComments(@PathVariable("sellerId") Long sellerId) {
        List<Map<String, Object>> comments = commentService.getCommentsBySellerId(sellerId);
        return Result.success(comments);
    }
}
