package com.minichat.message.dto.content;

import lombok.Data;

@Data
public class FileContent {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件地址
     */
    private String url;

    /**
     * 文件大小(Byte)
     */
    private Long size;

    /**
     * 文件后缀
     */
    private String suffix;

}
