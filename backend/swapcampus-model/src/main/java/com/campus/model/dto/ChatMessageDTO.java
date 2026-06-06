package com.campus.model.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {

    private Long toUid;

    private String content;

    private Integer msgType;

    private String imageUrl;
}
