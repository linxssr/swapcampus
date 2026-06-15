package com.campus.mapper;

import com.campus.model.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE student_id = #{studentId}")
    User findByStudentId(String studentId);

    @Select("SELECT * FROM user WHERE user_id = #{userId}")
    User selectById(Long userId);

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

    @Update("UPDATE user SET credit_score = credit_score + #{score}, update_time = NOW() WHERE user_id = #{userId}")
    int updateCreditScore(@Param("userId") Long userId, @Param("score") Integer score);

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

    /**
     * 批量获取订单的商品名称
     * @param orderIds 订单ID列表
     * @return 包含 order_id 和 item_title 的 Map 列表
     */
    @Select({
            "<script>",
            "SELECT o.order_id, i.title AS item_title",
            "FROM orders o",
            "LEFT JOIN item i ON o.item_id = i.item_id",
            "WHERE o.order_id IN",
            "<foreach collection='orderIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<Map<String, Object>> getOrderItems(@Param("orderIds") List<Long> orderIds);

    /**
     * 批量查询商品信息
     * @param itemIds 商品ID列表
     * @return 商品列表
     */
    @Select({
            "<script>",
            "SELECT item_id, user_id, title, price, cover_url, publish_status, create_time",  // ← 添加 user_id
            "FROM item",
            "WHERE item_id IN",
            "<foreach collection='itemIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<Map<String, Object>> getItemsByIds(@Param("itemIds") List<Long> itemIds);

    // 更新头像
    @Update("UPDATE user SET avatar = #{avatarUrl}, update_time = NOW() WHERE user_id = #{userId}")
    int updateAvatar(@Param("userId") Long userId, @Param("avatarUrl") String avatarUrl);

    /**
     * 查询用户列表（分页 + 搜索 + 状态筛选）
     */
    @Select({
            "<script>",
            "SELECT user_id, student_id, username, phone, credit_score, status, create_time",
            "FROM user",
            "WHERE 1=1",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND (student_id LIKE CONCAT('%', #{keyword}, '%') OR username LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "<if test='status != null'>",
            "AND status = #{status}",
            "</if>",
            "ORDER BY user_id DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"
    })
    List<Map<String, Object>> selectUserList(@Param("keyword") String keyword,
                                             @Param("status") Integer status,
                                             @Param("offset") int offset,
                                             @Param("size") int size);

    /**
     * 统计用户总数
     */
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM user",
            "WHERE 1=1",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND (student_id LIKE CONCAT('%', #{keyword}, '%') OR username LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "<if test='status != null'>",
            "AND status = #{status}",
            "</if>",
            "</script>"
    })
    int countUserList(@Param("keyword") String keyword,
                      @Param("status") Integer status);


    // 检查今天是否已签到
    @Select("SELECT COUNT(*) FROM sign_record WHERE user_id = #{userId} AND sign_date = #{today}")
    int checkSignToday(@Param("userId") Long userId, @Param("today") LocalDate today);

    // 获取上次签到日期
    @Select("SELECT sign_date FROM sign_record WHERE user_id = #{userId} ORDER BY sign_date DESC LIMIT 1")
    LocalDate getLastSignDate(@Param("userId") Long userId);

    // 获取上次连续签到天数
    @Select("SELECT continuous_days FROM sign_record WHERE user_id = #{userId} ORDER BY sign_date DESC LIMIT 1")
    Integer getLastContinuousDays(@Param("userId") Long userId);

    // 插入签到记录
    @Insert("INSERT INTO sign_record(user_id, sign_date, continuous_days, create_time) VALUES(#{userId}, #{today}, #{continuousDays}, NOW())")
    int insertSignRecord(@Param("userId") Long userId, @Param("today") LocalDate today, @Param("continuousDays") int continuousDays);


    // 获取用户信用分
    @Select("SELECT credit_score FROM user WHERE user_id = #{userId}")
    Integer getCreditScore(@Param("userId") Long userId);

    // 根据学号获取真实姓名
    @Select("SELECT real_name FROM student_real WHERE student_id = #{studentId}")
    String getRealNameByStudentId(@Param("studentId") String studentId);

    // 检查学号是否在对照表中存在
    @Select("SELECT COUNT(*) FROM student_real WHERE student_id = #{studentId}")
    int checkStudentIdExists(@Param("studentId") String studentId);
}