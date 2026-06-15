package com.campus.model.vo;

import com.campus.model.entity.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ItemVO extends BaseVO {

    private Long itemId;
    private Long userId;
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
    private String sellerName;
    private Integer sellerCreditScore;

    public static ItemVO fromEntity(Item item) {
        ItemVO vo = new ItemVO();
        vo.setItemId(item.getItemId());
        vo.setUserId(item.getUserId());
        vo.setCategoryId(item.getCategoryId());
        vo.setTitle(item.getTitle());
        vo.setDescription(item.getDescription());
        vo.setPrice(item.getPrice());
        vo.setQuality(item.getQuality());
        vo.setQualityDesc(getQualityDesc(item.getQuality()));
        vo.setCoverUrl(item.getCoverUrl());
        vo.setAuditStatus(item.getAuditStatus());
        vo.setAuditStatusDesc(getAuditStatusDesc(item.getAuditStatus()));
        vo.setPublishStatus(item.getPublishStatus());
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());
        return vo;
    }

    public static String getQualityDesc(Integer quality) {
        if (quality == null) return "";
        return switch (quality) {
            case 1 -> "全新";
            case 2 -> "几乎全新";
            case 3 -> "轻微使用痕迹";
            case 4 -> "明显使用痕迹";
            default -> "未知";
        };
    }

    public static String getAuditStatusDesc(Integer auditStatus) {
        if (auditStatus == null) return "";
        return switch (auditStatus) {
            case 0 -> "待审核";
            case 1 -> "审核通过";
            case 2 -> "审核驳回";
            default -> "未知";
        };
    }
}
