package com.campus.mapper;

import com.campus.model.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ItemMapper {
    @Select("SELECT * FROM item WHERE category_id = #{cid} AND audit_status = 1 AND publish_status = 1 ORDER BY create_time DESC")
    List<Item> listByCategoryId(@Param("cid") Long cid);

    @Select("SELECT * FROM item WHERE title LIKE CONCAT('%', #{key}, '%') AND audit_status = 1 AND publish_status = 1 ORDER BY create_time DESC")
    List<Item> searchByKeyword(@Param("key") String key);

    List<Item> filterItems(@Param("categoryId") Long categoryId,
                          @Param("minPrice") BigDecimal minPrice,
                          @Param("maxPrice") BigDecimal maxPrice,
                          @Param("quality") Integer quality);

    @Select("SELECT * FROM item WHERE item_id = #{itemId}")
    Item getById(@Param("itemId") Long itemId);
}

