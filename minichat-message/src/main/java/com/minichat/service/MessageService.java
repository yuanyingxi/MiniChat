package com.minichat.service;

import com.minichat.dto.SendMessageReq;
import com.minichat.entity.Message;
import com.minichat.vo.MessageVO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface MessageService {

    Long sendMessage(SendMessageReq req, Long fromId);

    List<MessageVO> getAllHistory();
    List<MessageVO> getPrivateHistory(Long targetId);
    List<MessageVO> getGroupHistory(Long targetId);
}