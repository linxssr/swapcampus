package com.campus.mapper;

import com.campus.model.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE student_id = #{studentId}")
    User findByStudentId(String studentId);

    @Select("SELECT * FROM user WHERE user_id = #{userId}")  // ← 检查这一行是否存在
    User findByUserId(Long userId);

    @Insert("INSERT INTO user(student_id, username, password, phone, avatar, credit_score, status, create_time, update_time) " +
            "VALUES(#{studentId}, #{username}, #{password}, #{phone}, #{avatar}, #{creditScore}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    int insert(User user);

    @Update("UPDATE user SET username = #{username}, phone = #{phone}, avatar = #{avatar}, update_time = #{updateTime} WHERE user_id = #{userId}")
    int update(User user);

    @Update("UPDATE user SET password = #{password}, update_time = #{updateTime} WHERE user_id = #{userId}")
    int updatePassword(@Param("password") String password,
                       @Param("updateTime") LocalDateTime updateTime,
                       @Param("userId") Long userId);

    /**
     * 查询用户发布的商品列表
     * ⚠️ TODO: 临时方案，等 module-m2-item 完成后，改用 ItemMapper
     */
    @Select("SELECT item_id, title, price, cover_url, publish_status, create_time FROM item WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Map<String, Object>> selectUserItems(@Param("userId") Long userId);

    /**
     * 查询用户的买家订单列表
     * ⚠️ TODO: 临时方案，等 module-m4-order 完成后，改用 OrderMapper
     */
    @Select("SELECT order_id, order_no, item_id, price, trade_type, order_status, create_time, finish_time FROM orders WHERE buyer_id = #{userId} ORDER BY create_time DESC")
    List<Map<String, Object>> selectBuyerOrders(@Param("userId") Long userId);

    /**
     * 查询用户的卖家订单列表
     * ⚠️ TODO: 临时方案，等 module-m4-order 完成后，改用 OrderMapper
     */
    @Select("SELECT order_id, order_no, item_id, price, trade_type, order_status, create_time, finish_time FROM orders WHERE seller_id = #{userId} ORDER BY create_time DESC")
    List<Map<String, Object>> selectSellerOrders(@Param("userId") Long userId);
}