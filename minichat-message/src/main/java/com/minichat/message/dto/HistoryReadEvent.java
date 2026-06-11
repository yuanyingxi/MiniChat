package com.minichat.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryReadEvent {

    private Integer chatType;

    private Long userId;

    private Long targetId;
}
