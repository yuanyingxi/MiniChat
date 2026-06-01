package com.minichat.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO { //返回给前端的数据

    private Long messageId;

    private Long fromId;

    private Long toId;

    private Integer messageType;

    private String content;

    private LocalDateTime createTime;
}