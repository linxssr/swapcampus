package com.campus.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long msgId;
    private Long fromUid;
    private Long toUid;
    private String content;
    private Integer msgType;
    private String imageUrl;
    private LocalDateTime sendTime;
    private Integer isRead;
}

