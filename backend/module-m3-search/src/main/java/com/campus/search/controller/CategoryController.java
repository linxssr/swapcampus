package com.campus.search.controller;

import com.campus.common.result.Result;
import com.campus.model.entity.Category;
import com.campus.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
public class CategoryController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/list")
    public Result<List<Category>> listAllCategories() {
        List<Category> categories = searchService.listAllCategories();
        return Result.success(categories);
    }
}


