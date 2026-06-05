package com.minichat.service;

import com.minichat.dto.SendMessageReq;
import com.minichat.entity.Message;
import com.minichat.vo.MessageVO;

import java.util.List;

public interface MessageService {

    Long sendMessage(
            SendMessageReq req,
            Long fromId
    );

    List<MessageVO> getHistory(Long conversationId);
}