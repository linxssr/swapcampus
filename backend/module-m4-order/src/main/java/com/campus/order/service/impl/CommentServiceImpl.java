package com.campus.order.service.impl;

import com.campus.common.exception.BusinessException;
import com.campus.mapper.CommentMapper;
import com.campus.mapper.OrderMapper;
import com.campus.mapper.UserMapper;
import com.campus.model.entity.Comment;
import com.campus.model.entity.Order;
import com.campus.order.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void addComment(Long buyerId, Long orderId, Integer score, String content) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(403, "只有买家可以评价");
        }

        if (order.getOrderStatus() != 3) {
            throw new BusinessException(400, "订单未完成，暂不可评价");
        }

        Comment existingComment = commentMapper.selectByOrderIdAndBuyerId(orderId, buyerId);
        if (existingComment != null) {
            throw new BusinessException(400, "该订单已评价，不能重复评价");
        }

        Comment comment = new Comment();
        comment.setOrderId(orderId);
        comment.setBuyerId(buyerId);
        comment.setSellerId(order.getSellerId());
        comment.setItemId(order.getItemId());
        comment.setContent(content);
        comment.setScore(score);

        commentMapper.insertComment(comment);

        int scoreChange = score >= 4 ? 5 : -5;
        userMapper.updateCreditScore(order.getSellerId(), scoreChange);
    }

    @Override
    public List<Map<String, Object>> getCommentsByItemId(Long itemId) {
        return commentMapper.selectByItemId(itemId);
    }

    @Override
    public Comment getCommentByOrderId(Long orderId) {
        return commentMapper.selectByOrderId(orderId);
    }

    @Override
    public List<Map<String, Object>> getCommentsBySellerId(Long sellerId) {
        return commentMapper.selectBySellerId(sellerId);
    }
}
