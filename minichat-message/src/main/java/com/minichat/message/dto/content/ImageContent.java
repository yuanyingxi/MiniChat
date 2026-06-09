package com.minichat.message.dto.content;

import lombok.Data;

@Data
public class ImageContent {

    /**
     * 原图地址
     */
    private String url;

    /**
     * 缩略图地址
     */
    private String thumbnailUrl;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 文件大小(Byte)
     */
    private Long size;

}
