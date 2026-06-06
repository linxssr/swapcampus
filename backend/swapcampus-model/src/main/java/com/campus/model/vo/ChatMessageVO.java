package com.campus.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    private Long msgId;

    private Long fromUid;

    private Long toUid;

    private String content;

    private Integer msgType;

    private String imageUrl;

    private LocalDateTime sendTime;

    private Integer isRead;
}
