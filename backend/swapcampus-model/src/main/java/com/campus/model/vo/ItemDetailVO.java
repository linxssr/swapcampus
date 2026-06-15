package com.campus.model.vo;

import com.campus.model.entity.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ItemDetailVO extends BaseVO {

    private Long itemId;
    private Long userId;
    private String sellerName;
    private Integer sellerCreditScore;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer quality;
    private String qualityDesc;
    private String coverUrl;
    private List<String> imageUrls;
    private Integer auditStatus;
    private String auditStatusDesc;
    private Integer publishStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isOwner;

    public static ItemDetailVO fromEntity(Item item, List<String> imageUrls, String sellerName,
                                          Integer sellerCreditScore, String categoryName, Long currentUserId) {
        ItemDetailVO vo = new ItemDetailVO();
        vo.setItemId(item.getItemId());
        vo.setUserId(item.getUserId());
        vo.setSellerName(sellerName);
        vo.setSellerCreditScore(sellerCreditScore);
        vo.setCategoryId(item.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setTitle(item.getTitle());
        vo.setDescription(item.getDescription());
        vo.setPrice(item.getPrice());
        vo.setQuality(item.getQuality());
        vo.setQualityDesc(ItemVO.getQualityDesc(item.getQuality()));
        vo.setCoverUrl(item.getCoverUrl());
        vo.setImageUrls(imageUrls);
        vo.setAuditStatus(item.getAuditStatus());
        vo.setAuditStatusDesc(ItemVO.getAuditStatusDesc(item.getAuditStatus()));
        vo.setPublishStatus(item.getPublishStatus());
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());
        vo.setIsOwner(currentUserId != null && currentUserId.equals(item.getUserId()));
        return vo;
    }
}
