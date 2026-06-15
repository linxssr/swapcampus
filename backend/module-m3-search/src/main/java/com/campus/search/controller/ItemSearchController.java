package com.campus.search.controller;

import com.campus.common.result.Result;
import com.campus.model.vo.ItemVO;
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
    public Result<List<ItemVO>> listItemsByCategory(@PathVariable Long cid) {
        return Result.success(searchService.listItemsByCategoryId(cid));
    }

    @GetMapping("/search")
    public Result<List<ItemVO>> searchItems(@RequestParam("key") String key) {
        return Result.success(searchService.searchItemsByKeyword(key));
    }

    @GetMapping("/filter")
    public Result<List<ItemVO>> filterItems(@RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) BigDecimal minPrice,
                                            @RequestParam(required = false) BigDecimal maxPrice,
                                            @RequestParam(required = false) Integer quality) {
        return Result.success(searchService.filterItems(categoryId, minPrice, maxPrice, quality));
    }
}
