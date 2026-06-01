package com.minichat.service;

import com.minichat.dto.SendMessageReq;

public interface MessageService {

    Long sendMessage(SendMessageReq req);
}