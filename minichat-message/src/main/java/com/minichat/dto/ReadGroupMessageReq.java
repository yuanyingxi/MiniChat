package com.minichat.dto;

import lombok.Data;

@Data
public class ReadGroupMessageReq {

    private Long groupId;

    private Long messageId;
}
