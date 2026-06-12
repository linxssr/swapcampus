package com.campus.search.service;

import com.campus.model.entity.Category;
import com.campus.model.entity.Item;
import com.campus.model.entity.ItemCollect;

import java.math.BigDecimal;
import java.util.List;

public interface SearchService {
    List<Category> listAllCategories();

    List<Item> listItemsByCategoryId(Long cid);

    List<Item> searchItemsByKeyword(String key);

    List<Item> filterItems(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer quality);

    void addCollect(Long userId, Long itemId);

    void cancelCollect(Long id, Long userId);

    List<ItemCollect> listMyCollects(Long userId);
}


