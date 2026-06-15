package com.campus.item.controller;

import com.campus.common.exception.BusinessException;
import com.campus.common.interceptor.JwtInterceptor;
import com.campus.common.result.Result;
import com.campus.item.service.ItemService;
import com.campus.model.dto.ItemPublishDTO;
import com.campus.model.dto.ItemUpdateDTO;
import com.campus.model.vo.ItemDetailVO;
import com.campus.model.vo.ItemVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/item")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/uploadImg")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file,
                                      HttpServletRequest request) {
        requireLogin(request);
        String url = itemService.uploadImage(file);
        return Result.success(url);
    }

    @PostMapping("/publish")
    public Result<ItemVO> publish(HttpServletRequest request,
                                  @Valid @RequestBody ItemPublishDTO dto) {
        Long userId = requireLogin(request);
        ItemVO vo = itemService.publish(userId, dto);
        return Result.success(vo);
    }

    @PutMapping("/update")
    public Result<ItemVO> update(HttpServletRequest request,
                                 @Valid @RequestBody ItemUpdateDTO dto) {
        Long userId = requireLogin(request);
        ItemVO vo = itemService.update(userId, dto);
        return Result.success(vo);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(HttpServletRequest request,
                               @PathVariable("id") Long itemId) {
        Long userId = requireLogin(request);
        itemService.delete(userId, itemId);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    public Result<ItemDetailVO> getDetail(HttpServletRequest request,
                                          @PathVariable("id") Long itemId) {
        Long currentUserId = getUserIdOrNull(request);
        ItemDetailVO detail = itemService.getDetail(itemId, currentUserId);
        return Result.success(detail);
    }

    @GetMapping("/my")
    public Result<List<ItemVO>> getMyItems(HttpServletRequest request) {
        Long userId = requireLogin(request);
        List<ItemVO> items = itemService.getMyItems(userId);
        return Result.success(items);
    }

    @GetMapping("/user/{userId}")
    public Result<List<ItemVO>> getUserItems(@PathVariable("userId") Long userId) {
        List<ItemVO> items = itemService.getMyItems(userId);
        return Result.success(items);
    }

    private Long requireLogin(HttpServletRequest request) {
        Long userId = getUserIdOrNull(request);
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }

    private Long getUserIdOrNull(HttpServletRequest request) {
        Object userId = request.getAttribute(JwtInterceptor.ATTR_USER_ID);
        if (userId == null) {
            return null;
        }
        if (userId instanceof Long) {
            return (Long) userId;
        }
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return Long.parseLong(userId.toString());
    }
}
