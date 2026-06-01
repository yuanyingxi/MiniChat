package com.minichat.dto;

import lombok.Data;

@Data
public class SendMessageReq {

    private Integer chatType;

    private Long toId;

    private Integer messageType;

    private String content;

    private Long clientSendTime;
}