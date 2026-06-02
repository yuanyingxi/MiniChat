package com.minichat.controller;

import com.minichat.dto.SendMessageReq;
import com.minichat.entity.Message;
import com.minichat.service.MessageService;
import com.minichat.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public Long send(@RequestBody SendMessageReq req) {
        return messageService.sendMessage(req);
    }

    @GetMapping("/history")
    public List<MessageVO> history(@RequestParam("conversationId") Long conversationId) {
        return messageService.getHistory(conversationId);
    }
}