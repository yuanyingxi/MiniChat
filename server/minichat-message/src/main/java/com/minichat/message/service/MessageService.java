package com.minichat.message.service;

import com.minichat.message.dto.SendMessageReq;
import com.minichat.message.dto.MessageVO;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageService {

    Long sendMessage(SendMessageReq req, Long fromId);

    List<MessageVO> getAllHistory();
    List<MessageVO> getPrivateHistory(Long userId, Long targetId, LocalDateTime startTime, LocalDateTime endTime);
    List<MessageVO> getGroupHistory(Long userId, Long targetId, LocalDateTime startTime, LocalDateTime endTime);
}