package com.minichat.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {

    private Long conversationId;

    private Integer conversationType;

    private Long targetId;

    private Object lastMessageContent;

    private LocalDateTime lastMessageTime;
}
