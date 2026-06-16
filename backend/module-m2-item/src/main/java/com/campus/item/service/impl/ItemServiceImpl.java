package com.campus.item.service.impl;

import com.campus.common.exception.BusinessException;
import com.campus.common.utils.MinIOUtil;
import com.campus.item.service.ItemService;
import com.campus.mapper.BrowseHistoryMapper;
import com.campus.mapper.CategoryMapper;
import com.campus.mapper.ItemImageMapper;
import com.campus.mapper.ItemMapper;
import com.campus.mapper.UserMapper;
import com.campus.model.dto.ItemPublishDTO;
import com.campus.model.dto.ItemUpdateDTO;
import com.campus.model.entity.Category;
import com.campus.model.entity.Item;
import com.campus.model.entity.ItemImage;
import com.campus.model.entity.User;
import com.campus.model.vo.ItemDetailVO;
import com.campus.model.vo.ItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private static final int AUDIT_STATUS_PENDING = 0;
    private static final int PUBLISH_STATUS_ONLINE = 1;

    private final MinIOUtil minIOUtil;
    private final ItemMapper itemMapper;
    private final ItemImageMapper itemImageMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final BrowseHistoryMapper browseHistoryMapper;

    public ItemServiceImpl(MinIOUtil minIOUtil,
                           ItemMapper itemMapper,
                           ItemImageMapper itemImageMapper,
                           UserMapper userMapper,
                           CategoryMapper categoryMapper,
                           BrowseHistoryMapper browseHistoryMapper) {
        this.minIOUtil = minIOUtil;
        this.itemMapper = itemMapper;
        this.itemImageMapper = itemImageMapper;
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.browseHistoryMapper = browseHistoryMapper;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        return minIOUtil.upload(file, "items");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ItemVO publish(Long userId, ItemPublishDTO dto) {
        User seller = userMapper.selectById(userId);
        if (seller == null) {
            throw new BusinessException(404, "用户不存在");
        }

        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(404, "商品分类不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        Item item = new Item();
        item.setUserId(userId);
        item.setCategoryId(dto.getCategoryId());
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setQuality(dto.getQuality());
        item.setCoverUrl(dto.getCoverUrl());
        item.setAuditStatus(AUDIT_STATUS_PENDING);
        item.setPublishStatus(PUBLISH_STATUS_ONLINE);
        item.setCreateTime(now);
        item.setUpdateTime(now);

        itemMapper.insert(item);
        saveItemImages(item.getItemId(), dto.getImageUrls());

        ItemVO vo = ItemVO.fromEntity(item);
        vo.setCategoryName(category.getCategoryName());
        vo.setSellerName(seller.getUsername());
        vo.setSellerCreditScore(seller.getCreditScore());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ItemVO update(Long userId, ItemUpdateDTO dto) {
        Item item = itemMapper.selectById(dto.getItemId());
        if (item == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权编辑此商品");
        }

        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(404, "商品分类不存在");
        }

        User seller = userMapper.selectById(userId);

        item.setCategoryId(dto.getCategoryId());
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setQuality(dto.getQuality());
        item.setCoverUrl(dto.getCoverUrl());
        item.setAuditStatus(AUDIT_STATUS_PENDING);
        item.setPublishStatus(PUBLISH_STATUS_ONLINE);
        item.setUpdateTime(LocalDateTime.now());

        itemMapper.update(item);
        itemImageMapper.deleteByItemId(item.getItemId());
        saveItemImages(item.getItemId(), dto.getImageUrls());

        ItemVO vo = ItemVO.fromEntity(item);
        vo.setCategoryName(category.getCategoryName());
        vo.setSellerName(seller != null ? seller.getUsername() : "");
        vo.setSellerCreditScore(seller != null ? seller.getCreditScore() : null);
        return vo;
    }

    @Override
    public void delete(Long userId, Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此商品");
        }

        itemMapper.removeById(itemId, LocalDateTime.now());
    }

    @Override
    public ItemDetailVO getDetail(Long itemId, Long currentUserId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(404, "商品不存在");
        }

        List<ItemImage> images = itemImageMapper.selectByItemId(itemId);
        List<String> imageUrls = images.stream()
                .map(ItemImage::getImageUrl)
                .collect(Collectors.toList());

        String sellerName = "";
        Integer sellerCreditScore = null;
        if (item.getUserId() != null) {
            User seller = userMapper.selectById(item.getUserId());
            if (seller != null) {
                sellerName = seller.getUsername();
                sellerCreditScore = seller.getCreditScore();
            }
        }

        String categoryName = "";
        if (item.getCategoryId() != null) {
            Category category = categoryMapper.selectById(item.getCategoryId());
            if (category != null) {
                categoryName = category.getCategoryName();
            }
        }

        return ItemDetailVO.fromEntity(item, imageUrls, sellerName, sellerCreditScore, categoryName, currentUserId);
    }

    @Override
    public List<ItemVO> getMyItems(Long userId) {
        List<Item> items = itemMapper.selectByUserId(userId);
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        User seller = userMapper.selectById(userId);

        return items.stream().map(item -> {
            ItemVO vo = ItemVO.fromEntity(item);
            vo.setSellerName(seller != null ? seller.getUsername() : "");
            vo.setSellerCreditScore(seller != null ? seller.getCreditScore() : null);
            return vo;
        }).collect(Collectors.toList());
    }

    private void saveItemImages(Long itemId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        List<ItemImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            ItemImage img = new ItemImage();
            img.setItemId(itemId);
            img.setImageUrl(imageUrls.get(i));
            img.setSort(i);
            images.add(img);
        }
        itemImageMapper.insertBatch(images);
    }

    @Override
    public void recordBrowse(Long userId, Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) return;
        browseHistoryMapper.upsert(userId, itemId, item.getCategoryId(), LocalDateTime.now());
    }

    @Override
    public List<ItemVO> getRecommendItems(Long userId, int limit) {
        // 取最近 30 条浏览，统计 top3 分类
        List<Map<String, Object>> topCats = browseHistoryMapper.topCategories(userId, 30);
        if (topCats == null || topCats.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> browsedIds = browseHistoryMapper.browsedItemIds(userId);
        Set<Long> browsedSet = new HashSet<>(browsedIds);

        // 计算每个分类应分配的数量（按出现次数权重）
        long total = topCats.stream()
                .mapToLong(m -> ((Number) m.get("cnt")).longValue())
                .sum();

        List<Long> topCatIds = topCats.stream()
                .limit(3)
                .map(m -> ((Number) m.get("categoryId")).longValue())
                .collect(Collectors.toList());

        List<ItemVO> result = new ArrayList<>();

        for (Map<String, Object> catRow : topCats.stream().limit(3).collect(Collectors.toList())) {
            long catId = ((Number) catRow.get("categoryId")).longValue();
            long cnt   = ((Number) catRow.get("cnt")).longValue();
            int quota  = (int) Math.max(1, Math.round((double) cnt / total * limit));

            List<Item> candidates = itemMapper.selectByCategory(catId);
            candidates.stream()
                    .filter(it -> !browsedSet.contains(it.getItemId()))
                    .filter(it -> !it.getUserId().equals(userId))
                    .limit(quota)
                    .forEach(it -> {
                        User seller = userMapper.selectById(it.getUserId());
                        Category cat = categoryMapper.selectById(it.getCategoryId());
                        ItemVO vo = ItemVO.fromEntity(it);
                        vo.setSellerName(seller != null ? seller.getUsername() : "");
                        vo.setSellerCreditScore(seller != null ? seller.getCreditScore() : null);
                        vo.setCategoryName(cat != null ? cat.getCategoryName() : "");
                        result.add(vo);
                    });
        }

        // 随机打散，避免全是同一分类
        Collections.shuffle(result);

        // 若结果不足，用最新商品补足（排除已浏览和自己的）
        if (result.size() < limit) {
            Set<Long> resultIds = result.stream().map(ItemVO::getItemId).collect(Collectors.toSet());
            List<ItemVO> fallback = itemMapper.filterItems(null, null, null, null);
            if (fallback == null) fallback = Collections.emptyList();
            fallback.stream()
                    .filter(it -> !browsedSet.contains(it.getItemId()))
                    .filter(it -> !it.getUserId().equals(userId))
                    .filter(it -> !resultIds.contains(it.getItemId()))
                    .limit((long) limit - result.size())
                    .forEach(result::add);
        }

        return result.stream().limit(limit).collect(Collectors.toList());
    }
}
