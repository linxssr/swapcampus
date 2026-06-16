package com.campus.search.service;

import com.campus.model.entity.Category;
import com.campus.model.vo.CollectVO;
import com.campus.model.vo.ItemVO;

import java.math.BigDecimal;
import java.util.List;

public interface SearchService {
    List<Category> listAllCategories();

    List<ItemVO> listItemsByCategoryId(Long cid);

    List<ItemVO> searchItemsByKeyword(String key);

    List<ItemVO> filterItems(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer quality);

    void addCollect(Long userId, Long itemId);

    void cancelCollect(Long id, Long userId);

    List<CollectVO> listMyCollects(Long userId);
}


