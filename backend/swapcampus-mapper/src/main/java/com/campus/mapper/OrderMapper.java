package com.campus.mapper;

import com.campus.model.entity.Order;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders(order_no, buyer_id, seller_id, item_id, price, trade_type, order_status, create_time) " +
            "VALUES(#{orderNo}, #{buyerId}, #{sellerId}, #{itemId}, #{price}, #{tradeType}, #{orderStatus}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    void insert(Order order);

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(String orderNo);

    @Update("UPDATE orders SET order_status = #{status} WHERE order_no = #{orderNo}")
    void updateOrderStatus(@Param("orderNo") String orderNo, @Param("status") Integer status);

    @Update("UPDATE orders SET finish_time = #{finishTime} WHERE order_no = #{orderNo}")
    void updateFinishTime(@Param("orderNo") String orderNo, @Param("finishTime") LocalDateTime finishTime);

    @Select("SELECT * FROM orders WHERE order_id = #{orderId}")
    Order selectById(Long orderId);

    @Select("SELECT COUNT(*) FROM orders WHERE order_no LIKE CONCAT('ORD', #{dateStr}, '%')")
    Long countOrdersByDate(@Param("dateStr") String dateStr);

    @Select("""
        SELECT o.order_id AS orderId, o.order_no AS orderNo, o.item_id AS itemId,
               o.seller_id AS sellerId, o.price, o.trade_type AS tradeType,
               o.order_status AS orderStatus, o.create_time AS createTime, o.finish_time AS finishTime,
               i.title AS itemTitle, i.cover_url AS itemCover,
               u.username AS peerName
        FROM orders o
        LEFT JOIN item i ON i.item_id = o.item_id
        LEFT JOIN `user` u ON u.user_id = o.seller_id
        WHERE o.buyer_id = #{userId}
        ORDER BY o.create_time DESC
    """)
    List<Map<String, Object>> selectBuyerOrders(@Param("userId") Long userId);

    @Select("""
        SELECT o.order_id AS orderId, o.order_no AS orderNo, o.item_id AS itemId,
               o.buyer_id AS buyerId, o.price, o.trade_type AS tradeType,
               o.order_status AS orderStatus, o.create_time AS createTime, o.finish_time AS finishTime,
               i.title AS itemTitle, i.cover_url AS itemCover,
               u.username AS peerName
        FROM orders o
        LEFT JOIN item i ON i.item_id = o.item_id
        LEFT JOIN `user` u ON u.user_id = o.buyer_id
        WHERE o.seller_id = #{userId}
        ORDER BY o.create_time DESC
    """)
    List<Map<String, Object>> selectSellerOrders(@Param("userId") Long userId);
}
