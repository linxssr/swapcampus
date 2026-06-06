package com.campus.chat.service.impl;

import com.campus.chat.service.ChatService;
import com.campus.common.utils.MinIOUtil;
import com.campus.mapper.ChatMessageMapper;
import com.campus.model.dto.ChatMessageDTO;
import com.campus.model.entity.ChatMessage;
import com.campus.model.vo.ChatConversationVO;
import com.campus.model.vo.ChatMessageVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private static final int MESSAGE_TYPE_TEXT = 1;

    private final ChatMessageMapper chatMessageMapper;

    private final MinIOUtil minIOUtil;

    public ChatServiceImpl(ChatMessageMapper chatMessageMapper, MinIOUtil minIOUtil) {
        this.chatMessageMapper = chatMessageMapper;
        this.minIOUtil = minIOUtil;
    }

    @Override
    public ChatMessageVO saveMessage(Long fromUid, ChatMessageDTO chatMessageDTO) {
        validateMessage(fromUid, chatMessageDTO);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFromUid(fromUid);
        chatMessage.setToUid(chatMessageDTO.getToUid());
        chatMessage.setContent(chatMessageDTO.getContent());
        chatMessage.setMsgType(chatMessageDTO.getMsgType() == null ? MESSAGE_TYPE_TEXT : chatMessageDTO.getMsgType());
        chatMessage.setImageUrl(chatMessageDTO.getImageUrl());
        chatMessage.setSendTime(LocalDateTime.now());
        chatMessage.setIsRead(0);

        chatMessageMapper.insert(chatMessage);
        return toVO(chatMessage);
    }

    @Override
    public List<ChatMessageVO> getHistory(Long fromUid, Long toUid) {
        if (fromUid == null || toUid == null) {
            throw new IllegalArgumentException("聊天用户不能为空");
        }
        chatMessageMapper.markRead(fromUid, toUid);
        return chatMessageMapper.selectHistory(fromUid, toUid).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<ChatConversationVO> getConversations(Long uid) {
        if (uid == null) {
            throw new IllegalArgumentException("当前用户不能为空");
        }
        return chatMessageMapper.selectConversations(uid);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("仅支持图片文件");
        }
        return minIOUtil.upload(file, "chat");
    }

    @Override
    public void markMessageRead(Long currentUid, Long msgId) {
        if (currentUid == null || msgId == null) {
            throw new IllegalArgumentException("消息或当前用户不能为空");
        }
        chatMessageMapper.markReadByMsgId(msgId, currentUid);
    }

    private void validateMessage(Long fromUid, ChatMessageDTO chatMessageDTO) {
        if (fromUid == null) {
            throw new IllegalArgumentException("发送方不能为空");
        }
        if (chatMessageDTO == null || chatMessageDTO.getToUid() == null) {
            throw new IllegalArgumentException("接收方不能为空");
        }
        if (!StringUtils.hasText(chatMessageDTO.getContent()) && !StringUtils.hasText(chatMessageDTO.getImageUrl())) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
    }

    private ChatMessageVO toVO(ChatMessage chatMessage) {
        ChatMessageVO chatMessageVO = new ChatMessageVO();
        chatMessageVO.setMsgId(chatMessage.getMsgId());
        chatMessageVO.setFromUid(chatMessage.getFromUid());
        chatMessageVO.setToUid(chatMessage.getToUid());
        chatMessageVO.setContent(chatMessage.getContent());
        chatMessageVO.setMsgType(chatMessage.getMsgType());
        chatMessageVO.setImageUrl(chatMessage.getImageUrl());
        chatMessageVO.setSendTime(chatMessage.getSendTime());
        chatMessageVO.setIsRead(chatMessage.getIsRead());
        return chatMessageVO;
    }
}
