package com.minichat.dto;

import lombok.Data;

@Data
public class SendMessageReq {

    private Integer chatType;// 私聊 or 群聊

    private Long toId;

    private Integer messageType;// 消息类型：文本、图片(url)、视频(url)...

    private String content;
}