package com.campus.mapper;

import com.campus.model.entity.Category;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface CategoryMapper {

    /**
     * M3：获取启用分类列表
     */
    @Select("SELECT * FROM category WHERE status = 1 ORDER BY sort ASC")
    List<Category> listAll();

    /**
     * 获取所有分类
     */
    @Select("SELECT * FROM category ORDER BY sort ASC")
    List<Map<String, Object>> selectAllCategories();

    /**
     * 根据ID获取分类
     */
    @Select("SELECT * FROM category WHERE category_id = #{categoryId}")
    Map<String, Object> selectCategoryById(Long categoryId);

    /**
     * 新增分类（使用实体类）
     */
    @Insert("INSERT INTO category(category_name, sort, status) VALUES(#{categoryName}, #{sort}, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "categoryId", keyColumn = "category_id")
    int insertCategory(Category category);

    /**
     * 修改分类
     */
    @Update("UPDATE category SET category_name = #{categoryName}, sort = #{sort} WHERE category_id = #{categoryId}")
    int updateCategory(Category category);

    /**
     * 启用/禁用分类
     */
    @Update("UPDATE category SET status = #{status} WHERE category_id = #{categoryId}")
    int updateCategoryStatus(@Param("categoryId") Long categoryId, @Param("status") Integer status);
}