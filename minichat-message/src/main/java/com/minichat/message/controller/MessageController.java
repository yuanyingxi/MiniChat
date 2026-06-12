package com.minichat.message.controller;

import com.minichat.message.service.MessageService;
import com.minichat.message.dto.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/history/all")
    public List<MessageVO> getAllHistory() {
        return messageService.getAllHistory();
    }

    @GetMapping("/history/private")
    public List<MessageVO> getPrivateHistory(@RequestParam("targetId") Long targetId) {
        return messageService.getPrivateHistory(targetId);
    }

    @GetMapping("/history/group")
    public List<MessageVO> getGroupHistory(@RequestParam("targetId") Long targetId) {
        return messageService.getGroupHistory(targetId);
    }
}