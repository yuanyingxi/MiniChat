package com.minichat.service;

import com.minichat.dto.SendMessageReq;
import com.minichat.entity.Message;

import java.util.List;

public interface MessageService {

    Long sendMessage(SendMessageReq req);

    List<Message> getHistory(Long userId);
}