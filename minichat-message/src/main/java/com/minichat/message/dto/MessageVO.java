package com.minichat.message.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long Id;

    private Long fromId;

    private Long toId;

    private Integer messageType;

    private Object content;

    private LocalDateTime createTime;
}