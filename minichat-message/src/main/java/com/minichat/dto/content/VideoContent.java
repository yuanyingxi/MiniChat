package com.minichat.dto.content;

import lombok.Data;

@Data
public class VideoContent {

    /**
     * 视频地址
     */
    private String url;

    /**
     * 封面图
     */
    private String coverUrl;

    /**
     * 时长(秒)
     */
    private Integer duration;

    /**
     * 文件大小(Byte)
     */
    private Long size;

}
