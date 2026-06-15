package com.campus.mapper;

import com.campus.model.entity.Admin;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {

    /**
     * 根据管理员账号查询
     */
    @Select("SELECT * FROM admin WHERE admin_name = #{adminName}")
    Admin findByAdminName(String adminName);

    /**
     * 封禁/解封用户
     * @param userId 用户ID
     * @param status 0-封禁 1-正常
     */
    @Update("UPDATE user SET status = #{status} WHERE user_id = #{userId}")
    int updateUserStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 审核商品
     * @param itemId 商品ID
     * @param auditStatus 审核状态：1-通过 2-驳回
     * @param adminId 管理员ID
     * @param updateTime 更新时间
     * 暂时放在adminMapper
     */
    @Update("UPDATE item SET audit_status = #{auditStatus}, audit_admin_id = #{adminId}, update_time = #{updateTime} WHERE item_id = #{itemId}")
    int auditItem(@Param("itemId") Long itemId,
                  @Param("auditStatus") Integer auditStatus,
                  @Param("adminId") Long adminId,
                  @Param("updateTime") LocalDateTime updateTime);

    /**
     * 添加管理员
     */
    @Insert("INSERT INTO admin(admin_name, password, real_name, phone, status, create_time) " +
            "VALUES(#{adminName}, #{password}, #{realName}, #{phone}, 1, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "adminId", keyColumn = "admin_id")
    int insertAdmin(Admin admin);

    /**
     * 查询商品列表（分页）
     */
    @Select({
            "<script>",
            "SELECT i.item_id, i.user_id, i.category_id, i.title, i.description, i.price, i.quality,",
            "i.cover_url, i.audit_status, i.publish_status, i.create_time, i.update_time,",
            "u.username, c.category_name",
            "FROM item i",
            "LEFT JOIN user u ON i.user_id = u.user_id",
            "LEFT JOIN category c ON i.category_id = c.category_id",
            "WHERE 1=1",
            "<if test='auditStatus != null'>",
            "AND i.audit_status = #{auditStatus}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",  // ← 新增
            "AND i.title LIKE CONCAT('%', #{keyword}, '%')",     // ← 新增
            "</if>",                                              // ← 新增
            "ORDER BY i.create_time DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"
    })
    List<Map<String, Object>> selectItemList(@Param("auditStatus") Integer auditStatus,
                                             @Param("keyword") String keyword,
                                             @Param("offset") int offset,
                                             @Param("size") int size);

    /**
     * 统计商品总数
     */
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM item i",
            "WHERE 1=1",
            "<if test='auditStatus != null'>",
            "AND i.audit_status = #{auditStatus}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",  // ← 新增
            "AND i.title LIKE CONCAT('%', #{keyword}, '%')",     // ← 新增
            "</if>",                                              // ← 新增
            "</script>"
    })
    int countItemList(@Param("auditStatus") Integer auditStatus,
                      @Param("keyword") String keyword);

    /**
     * 查询举报列表（分页 + 筛选 + 搜索）
     */
    @Select({
            "<script>",
            "SELECT r.report_id, r.report_user, r.item_id, r.reason, r.handle_status, r.create_time, r.handle_result,",
            "u.username as report_user_name, i.title as item_title",
            "FROM report r",
            "LEFT JOIN user u ON r.report_user = u.user_id",
            "LEFT JOIN item i ON r.item_id = i.item_id",
            "WHERE 1=1",
            "<if test='handleStatus != null'>",
            "AND r.handle_status = #{handleStatus}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND (u.username LIKE CONCAT('%', #{keyword}, '%') OR ",
            "i.title LIKE CONCAT('%', #{keyword}, '%') OR ",
            "r.reason LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "ORDER BY r.create_time DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"
    })
    List<Map<String, Object>> selectReportList(@Param("handleStatus") Integer handleStatus,
                                               @Param("keyword") String keyword,
                                               @Param("offset") int offset,
                                               @Param("size") int size);
    /**
     * 统计举报总数
     */
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM report r",
            "WHERE 1=1",
            "<if test='handleStatus != null'>",
            "AND r.handle_status = #{handleStatus}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND (EXISTS (SELECT 1 FROM user u WHERE u.user_id = r.report_user AND u.username LIKE CONCAT('%', #{keyword}, '%')) OR ",
            "EXISTS (SELECT 1 FROM item i WHERE i.item_id = r.item_id AND i.title LIKE CONCAT('%', #{keyword}, '%')) OR ",
            "r.reason LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "</script>"
    })
    int countReportList(@Param("handleStatus") Integer handleStatus,
                        @Param("keyword") String keyword);


    /**
     * 查询分类列表（分页 + 搜索）
     */
    @Select({
            "<script>",
            "SELECT category_id, category_name, sort, status",
            "FROM category",
            "WHERE 1=1",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND category_name LIKE CONCAT('%', #{keyword}, '%')",
            "</if>",
            "ORDER BY sort ASC, category_id ASC",
            "LIMIT #{offset}, #{size}",
            "</script>"
    })
    List<Map<String, Object>> selectCategoryList(@Param("keyword") String keyword,
                                                 @Param("offset") int offset,
                                                 @Param("size") int size);

    /**
     * 统计分类总数
     */
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM category",
            "WHERE 1=1",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND category_name LIKE CONCAT('%', #{keyword}, '%')",
            "</if>",
            "</script>"
    })
    int countCategoryList(@Param("keyword") String keyword);

    /**
     * 修改分类状态
     */
    @Update("UPDATE category SET status = #{status} WHERE category_id = #{categoryId}")
    int updateCategoryStatus(@Param("categoryId") Long categoryId, @Param("status") Integer status);


// ========== 仪表盘统计 ==========

    @Select("SELECT COUNT(*) FROM item WHERE audit_status = 0")
    int countPendingItems();

    @Select("SELECT COUNT(*) FROM report WHERE handle_status = 0")
    int countPendingReports();

    @Select("SELECT COUNT(*) FROM user")
    int countTotalUsers();

    @Select("SELECT COUNT(*) FROM item")
    int countTotalItems();

    @Select("SELECT COUNT(*) FROM user WHERE DATE(create_time) = CURDATE()")
    int countTodayNewUsers();

    @Select("SELECT COUNT(*) FROM item WHERE DATE(create_time) = CURDATE()")
    int countTodayNewItems();

    @Select("SELECT COUNT(*) FROM orders WHERE DATE(create_time) = CURDATE()")
    int countTodayOrders();

    @Select("SELECT SUM(price) FROM orders WHERE order_status = 3 AND DATE(create_time) = CURDATE()")
    BigDecimal getTodayIncome();

// ========== 趋势数据 ==========

    @Select("SELECT COUNT(*) FROM user WHERE DATE(create_time) = #{date}")
    int countNewUsersByDate(@Param("date") String date);

    @Select("SELECT COUNT(*) FROM item WHERE DATE(create_time) = #{date}")
    int countNewItemsByDate(@Param("date") String date);

// ========== 订单状态分布 ==========

    @Select({
            "SELECT order_status as status, COUNT(*) as count",
            "FROM orders",
            "GROUP BY order_status"
    })
    List<Map<String, Object>> getOrderStatusStat();

// ========== 分类统计 ==========

    @Select({
            "SELECT c.category_id, c.category_name, COUNT(i.item_id) as count",
            "FROM category c",
            "LEFT JOIN item i ON c.category_id = i.category_id AND i.publish_status = 1",
            "GROUP BY c.category_id",
            "ORDER BY count DESC",
            "LIMIT 5"
    })
    List<Map<String, Object>> getCategoryStats();


    // 获取用户信用分
    @Select("SELECT credit_score FROM user WHERE user_id = #{userId}")
    Integer getCreditScore(@Param("userId") Long userId);

    // 更新用户信用分
    @Update("UPDATE user SET credit_score = #{newScore}, update_time = NOW() WHERE user_id = #{userId}")
    int updateCreditScore(@Param("userId") Long userId, @Param("newScore") int newScore);

    /**
     * 查询订单列表（分页 + 筛选 + 搜索）
     */
    @Select({
            "<script>",
            "SELECT o.order_id, o.order_no, o.buyer_id, o.seller_id, o.item_id, o.price,",
            "o.trade_type, o.order_status, o.create_time, o.finish_time,",
            "i.title as item_title,",
            "ub.username as buyer_name,",
            "us.username as seller_name",
            "FROM orders o",
            "LEFT JOIN item i ON o.item_id = i.item_id",
            "LEFT JOIN user ub ON o.buyer_id = ub.user_id",
            "LEFT JOIN user us ON o.seller_id = us.user_id",
            "WHERE 1=1",
            "<if test='orderStatus != null'>",
            "AND o.order_status = #{orderStatus}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND (o.order_no LIKE CONCAT('%', #{keyword}, '%') OR ",
            "i.title LIKE CONCAT('%', #{keyword}, '%') OR ",
            "ub.username LIKE CONCAT('%', #{keyword}, '%') OR ",
            "us.username LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "ORDER BY o.create_time DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"
    })
    List<Map<String, Object>> selectOrderList(@Param("orderStatus") Integer orderStatus,
                                              @Param("keyword") String keyword,
                                              @Param("offset") int offset,
                                              @Param("size") int size);

    /**
     * 统计订单总数
     */
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM orders o",
            "WHERE 1=1",
            "<if test='orderStatus != null'>",
            "AND o.order_status = #{orderStatus}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "AND (o.order_no LIKE CONCAT('%', #{keyword}, '%') OR ",
            "EXISTS (SELECT 1 FROM item i WHERE i.item_id = o.item_id AND i.title LIKE CONCAT('%', #{keyword}, '%')) OR ",
            "EXISTS (SELECT 1 FROM user u WHERE u.user_id = o.buyer_id AND u.username LIKE CONCAT('%', #{keyword}, '%')) OR ",
            "EXISTS (SELECT 1 FROM user u WHERE u.user_id = o.seller_id AND u.username LIKE CONCAT('%', #{keyword}, '%')))",
            "</if>",
            "</script>"
    })
    int countOrderList(@Param("orderStatus") Integer orderStatus,
                       @Param("keyword") String keyword);

    /**
     * 按状态统计订单数量
     */
    @Select("SELECT COUNT(*) FROM orders WHERE order_status = #{status}")
    int countOrdersByStatus(@Param("status") int status);
}