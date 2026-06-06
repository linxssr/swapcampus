package com.campus.model.entity;

import lombok.Data;

@Data
public class ItemImage {
    private Long imageId;
    private Long itemId;
    private String imageUrl;
    private Integer sort;
}

