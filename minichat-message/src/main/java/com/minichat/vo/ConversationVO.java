package com.minichat.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {

    private Long Id;

    private Integer conversationType;

    private Long targetId;

    private Object lastMessageContent;

    private LocalDateTime lastMessageTime;
}
