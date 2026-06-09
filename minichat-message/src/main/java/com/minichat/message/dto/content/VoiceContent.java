package com.minichat.message.dto.content;

import lombok.Data;

@Data
public class VoiceContent {

    /**
     * 语音地址
     */
    private String url;

    /**
     * 时长(秒)
     */
    private Integer duration;

    /**
     * 文件大小(Byte)
     */
    private Long size;

}
