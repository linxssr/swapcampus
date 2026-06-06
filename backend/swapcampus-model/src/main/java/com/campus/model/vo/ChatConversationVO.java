package com.campus.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatConversationVO {

    private Long peerUid;

    private String peerName;

    private String peerAvatar;

    private Long lastMsgId;

    private String lastContent;

    private Integer lastMsgType;

    private LocalDateTime lastSendTime;

    private Integer unreadCount;
}
