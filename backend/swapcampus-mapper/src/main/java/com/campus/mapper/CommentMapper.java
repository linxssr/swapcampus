package com.campus.mapper;

import com.campus.model.entity.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper {

    @Insert("INSERT INTO comment(order_id, buyer_id, seller_id, item_id, content, score, create_time) " +
            "VALUES(#{orderId}, #{buyerId}, #{sellerId}, #{itemId}, #{content}, #{score}, NOW())")
    void insertComment(Comment comment);

    @Select("""
        SELECT c.comment_id AS commentId, c.order_id AS orderId, c.buyer_id AS buyerId,
               c.seller_id AS sellerId, c.item_id AS itemId, c.content, c.score,
               c.create_time AS createTime, u.username AS buyerName
        FROM comment c
        LEFT JOIN `user` u ON u.user_id = c.buyer_id
        WHERE c.item_id = #{itemId}
        ORDER BY c.create_time DESC
    """)
    List<Map<String, Object>> selectByItemId(Long itemId);

    @Select("SELECT * FROM comment WHERE order_id = #{orderId}")
    Comment selectByOrderId(Long orderId);

    @Select("SELECT * FROM comment WHERE order_id = #{orderId} AND buyer_id = #{buyerId}")
    Comment selectByOrderIdAndBuyerId(Long orderId, Long buyerId);

    @Select("""
        SELECT c.comment_id AS commentId, c.order_id AS orderId, c.buyer_id AS buyerId,
               c.seller_id AS sellerId, c.item_id AS itemId, c.content, c.score,
               c.create_time AS createTime, u.username AS buyerName, i.title AS itemTitle
        FROM comment c
        LEFT JOIN `user` u ON u.user_id = c.buyer_id
        LEFT JOIN item i ON i.item_id = c.item_id
        WHERE c.seller_id = #{sellerId}
        ORDER BY c.create_time DESC
    """)
    List<Map<String, Object>> selectBySellerId(Long sellerId);
}
