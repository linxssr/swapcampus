package com.campus.model.entity;

import lombok.Data;

@Data
public class Category {
    private Long categoryId;
    private String categoryName;
    private Integer sort;
    private Integer status;
}

