package com.campus.admin.dto;

public class CategoryAddRequest {
    private String name;
    private Integer sort;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}