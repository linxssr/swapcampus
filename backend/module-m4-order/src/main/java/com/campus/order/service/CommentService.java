package com.campus.order.service;

import com.campus.model.entity.Comment;
import java.util.List;
import java.util.Map;

public interface CommentService {
    void addComment(Long buyerId, Long orderId, Integer score, String content);
    List<Map<String, Object>> getCommentsByItemId(Long itemId);
    Comment getCommentByOrderId(Long orderId);
    List<Map<String, Object>> getCommentsBySellerId(Long sellerId);
}
