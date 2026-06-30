package com.minichat.message.entity;


import lombok.Data;

@Data
public class MessageArchive {

    private Long id;

    private Long conversationId;

    private Integer chatType;

    private Long fromId;

    private Long toId;

    private Integer messageType;

    private Integer status;

    private Long clientSendTime;

    private String content;

    private String updateTime;

    private String createTime;
}
