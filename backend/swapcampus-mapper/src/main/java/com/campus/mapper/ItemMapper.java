package com.campus.mapper;

import com.campus.model.entity.Item;
import com.campus.model.vo.ItemVO;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ItemMapper {

    @Insert("INSERT INTO item (user_id, category_id, title, description, price, quality, cover_url, " +
            "audit_status, publish_status, create_time, update_time) " +
            "VALUES (#{userId}, #{categoryId}, #{title}, #{description}, #{price}, #{quality}, " +
            "#{coverUrl}, #{auditStatus}, #{publishStatus}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "itemId", keyColumn = "item_id")
    int insert(Item item);

    @Update("UPDATE item SET category_id = #{categoryId}, title = #{title}, description = #{description}, " +
            "price = #{price}, quality = #{quality}, cover_url = #{coverUrl}, " +
            "audit_status = #{auditStatus}, publish_status = #{publishStatus}, " +
            "update_time = #{updateTime} WHERE item_id = #{itemId}")
    int update(Item item);

    @Update("UPDATE item SET publish_status = 0, update_time = #{updateTime} WHERE item_id = #{itemId}")
    int removeById(@Param("itemId") Long itemId, @Param("updateTime") LocalDateTime updateTime);

    @Select("SELECT * FROM item WHERE item_id = #{itemId}")
    Item selectById(@Param("itemId") Long itemId);

    @Select("SELECT * FROM item WHERE item_id = #{itemId}")
    Item getById(@Param("itemId") Long itemId);

    @Select("SELECT * FROM item WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Item> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM item WHERE audit_status = 0 AND publish_status = 1 ORDER BY create_time DESC")
    List<Item> selectPendingAudit();

    @Select("SELECT * FROM item WHERE category_id = #{categoryId} AND audit_status = 1 AND publish_status = 1 ORDER BY create_time DESC")
    List<Item> selectByCategory(@Param("categoryId") Long categoryId);

    List<ItemVO> listByCategoryId(@Param("cid") Long cid);

    List<ItemVO> searchByKeyword(@Param("key") String key);

    List<ItemVO> filterItems(@Param("categoryId") Long categoryId,
                             @Param("minPrice") BigDecimal minPrice,
                             @Param("maxPrice") BigDecimal maxPrice,
                             @Param("quality") Integer quality);

    @Update("UPDATE item SET publish_status = #{publishStatus}, update_time = NOW() WHERE item_id = #{itemId}")
    int updateById(Item item);
}
