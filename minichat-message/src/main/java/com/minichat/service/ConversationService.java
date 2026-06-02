package com.minichat.service;

import com.minichat.entity.Conversation;
import com.minichat.vo.ConversationVO;

import java.util.List;

public interface ConversationService {

    List<ConversationVO> list();
}
