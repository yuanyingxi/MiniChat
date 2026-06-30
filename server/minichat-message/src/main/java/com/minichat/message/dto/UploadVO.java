package com.minichat.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadVO {
    private String url;
    private String originalName;
}
