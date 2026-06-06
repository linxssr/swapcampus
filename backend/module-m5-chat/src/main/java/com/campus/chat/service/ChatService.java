package com.campus.chat.service;

import com.campus.model.dto.ChatMessageDTO;
import com.campus.model.vo.ChatConversationVO;
import com.campus.model.vo.ChatMessageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {

    ChatMessageVO saveMessage(Long fromUid, ChatMessageDTO chatMessageDTO);

    List<ChatMessageVO> getHistory(Long fromUid, Long toUid);

    List<ChatConversationVO> getConversations(Long uid);

    String uploadImage(MultipartFile file);

    void markMessageRead(Long currentUid, Long msgId);
}
