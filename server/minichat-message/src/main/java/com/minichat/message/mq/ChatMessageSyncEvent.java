package com.minichat.message.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ChatMessageSyncEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long conversationId;
    private Long fromId;
    private Long toId;
    private Integer chatType;
    private Integer messageType;
    private Integer status;
    private Long clientSendTime;
    private String content;
    private String createTime;   // 转为字符串传
    private String updateTime;
    private String operation;    // INSERT / UPDATE / DELETE
}