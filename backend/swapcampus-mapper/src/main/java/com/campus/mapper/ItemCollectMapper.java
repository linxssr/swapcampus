package com.campus.mapper;

import com.campus.model.entity.ItemCollect;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ItemCollectMapper {
    @Insert("INSERT INTO item_collect (user_id, item_id, create_time) VALUES (#{userId}, #{itemId}, NOW())")
    int insert(ItemCollect itemCollect);

    @Delete("DELETE FROM item_collect WHERE collect_id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT * FROM item_collect WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ItemCollect> listByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM item_collect WHERE user_id = #{userId} AND item_id = #{itemId}")
    ItemCollect getByUserIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);
}

