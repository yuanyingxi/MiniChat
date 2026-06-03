package com.minichat.dto;

import lombok.Data;

@Data
public class AckMessage {
    private Long messageId;
    private Long userId;
    private Integer type = 2; // ACK
}
