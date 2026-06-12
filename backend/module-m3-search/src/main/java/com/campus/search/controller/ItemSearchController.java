package com.campus.search.controller;

import com.campus.common.result.Result;
import com.campus.model.entity.Item;
import com.campus.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/item")
public class ItemSearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/cate/{cid}")
    public Result<List<Item>> listItemsByCategory(@PathVariable Long cid) {
        List<Item> items = searchService.listItemsByCategoryId(cid);
        return Result.success(items);
    }

    @GetMapping("/search")
    public Result<List<Item>> searchItems(@RequestParam("key") String key) {
        List<Item> items = searchService.searchItemsByKeyword(key);
        return Result.success(items);
    }

    @GetMapping("/filter")
    public Result<List<Item>> filterItems(@RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) BigDecimal minPrice,
                                          @RequestParam(required = false) BigDecimal maxPrice,
                                          @RequestParam(required = false) Integer quality) {
        List<Item> items = searchService.filterItems(categoryId, minPrice, maxPrice, quality);
        return Result.success(items);
    }
}
