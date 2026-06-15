package com.campus.mapper;

import com.campus.model.entity.ItemImage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ItemImageMapper {

    @Insert("<script>" +
            "INSERT INTO item_image (item_id, image_url, sort) VALUES " +
            "<foreach collection='list' item='img' separator=','>" +
            "(#{img.itemId}, #{img.imageUrl}, #{img.sort})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<ItemImage> images);

    @Delete("DELETE FROM item_image WHERE item_id = #{itemId}")
    int deleteByItemId(@Param("itemId") Long itemId);

    @Select("SELECT * FROM item_image WHERE item_id = #{itemId} ORDER BY sort ASC")
    List<ItemImage> selectByItemId(@Param("itemId") Long itemId);
}
