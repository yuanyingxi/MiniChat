package com.minichat.message.service;

import com.minichat.message.dto.SendMessageReq;
import com.minichat.message.dto.MessageVO;

import java.util.List;

public interface MessageService {

    Long sendMessage(SendMessageReq req, Long fromId);

    List<MessageVO> getAllHistory();
    List<MessageVO> getPrivateHistory(Long userId, Long targetId);
    List<MessageVO> getGroupHistory(Long userId, Long targetId);
}