package com.minichat.controller;

import com.minichat.entity.Conversation;
import com.minichat.service.ConversationService;
import com.minichat.vo.ConversationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/list")
    public List<ConversationVO> list() {

        return conversationService.list();
    }
}