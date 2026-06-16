package com.campus.mapper;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BrowseHistoryMapper {

    @Insert("INSERT INTO item_browse_history (user_id, item_id, category_id, browse_time) " +
            "VALUES (#{userId}, #{itemId}, #{categoryId}, #{browseTime}) " +
            "ON DUPLICATE KEY UPDATE browse_time = #{browseTime}")
    int upsert(@Param("userId") Long userId,
               @Param("itemId") Long itemId,
               @Param("categoryId") Long categoryId,
               @Param("browseTime") LocalDateTime browseTime);

    /** 返回用户最近 N 条浏览记录中各分类的出现次数，按次数降序 */
    @Select("SELECT category_id AS categoryId, COUNT(*) AS cnt " +
            "FROM (SELECT category_id FROM item_browse_history " +
            "       WHERE user_id = #{userId} ORDER BY browse_time DESC LIMIT #{limit}) t " +
            "GROUP BY category_id ORDER BY cnt DESC")
    List<Map<String, Object>> topCategories(@Param("userId") Long userId,
                                            @Param("limit") int limit);

    /** 返回用户已浏览过的所有 item_id，用于过滤推荐结果 */
    @Select("SELECT item_id FROM item_browse_history WHERE user_id = #{userId}")
    List<Long> browsedItemIds(@Param("userId") Long userId);
}
