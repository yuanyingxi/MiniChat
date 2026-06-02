package com.minichat.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long messageId;

    private Long fromId;

    private Long toId;

    private Integer messageType;

    private Object content;

    private LocalDateTime createTime;
}