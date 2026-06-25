package com.minichat.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryReadEvent {

    private Integer chatType;

    private Long userId;

    private Long targetId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
