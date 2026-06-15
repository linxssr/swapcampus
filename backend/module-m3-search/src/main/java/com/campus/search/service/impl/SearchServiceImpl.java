package com.campus.search.service.impl;

import com.campus.common.exception.BusinessException;
import com.campus.mapper.CategoryMapper;
import com.campus.mapper.ItemCollectMapper;
import com.campus.mapper.ItemMapper;
import com.campus.model.entity.Category;
import com.campus.model.entity.Item;
import com.campus.model.entity.ItemCollect;
import com.campus.model.vo.ItemVO;
import com.campus.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemCollectMapper itemCollectMapper;

    @Override
    public List<Category> listAllCategories() {
        return categoryMapper.listAll();
    }

    @Override
    public List<ItemVO> listItemsByCategoryId(Long cid) {
        return itemMapper.listByCategoryId(cid);
    }

    @Override
    public List<ItemVO> searchItemsByKeyword(String key) {
        return itemMapper.searchByKeyword(key);
    }

    @Override
    public List<ItemVO> filterItems(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer quality) {
        return itemMapper.filterItems(categoryId, minPrice, maxPrice, quality);
    }

    @Override
    @Transactional
    public void addCollect(Long userId, Long itemId) {
        Item item = itemMapper.getById(itemId);
        if (item == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (item.getAuditStatus() != 1 || item.getPublishStatus() != 1) {
            throw new BusinessException(400, "商品未上架");
        }
        ItemCollect existing = itemCollectMapper.getByUserIdAndItemId(userId, itemId);
        if (existing != null) {
            throw new BusinessException(400, "已收藏该商品");
        }
        ItemCollect itemCollect = new ItemCollect();
        itemCollect.setUserId(userId);
        itemCollect.setItemId(itemId);
        itemCollectMapper.insert(itemCollect);
    }

    @Override
    @Transactional
    public void cancelCollect(Long id, Long userId) {
        int result = itemCollectMapper.deleteByIdAndUserId(id, userId);
        if (result == 0) {
            throw new BusinessException(404, "收藏不存在");
        }
    }

    @Override
    public List<ItemCollect> listMyCollects(Long userId) {
        return itemCollectMapper.listByUserId(userId);
    }
}


