package com.campus.item.service;

import com.campus.model.dto.ItemPublishDTO;
import com.campus.model.dto.ItemUpdateDTO;
import com.campus.model.vo.ItemDetailVO;
import com.campus.model.vo.ItemVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {

    String uploadImage(MultipartFile file);

    ItemVO publish(Long userId, ItemPublishDTO dto);

    ItemVO update(Long userId, ItemUpdateDTO dto);

    void delete(Long userId, Long itemId);

    ItemDetailVO getDetail(Long itemId, Long currentUserId);

    List<ItemVO> getMyItems(Long userId);
}
