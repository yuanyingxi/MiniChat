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

    @GetMapping("/history/all")
    public List<MessageVO> getAllHistory() {
        return messageService.getAllHistory();
    }

    @GetMapping("/history/private")
    public List<MessageVO> getPrivateHistory(@RequestParam Long targetId) {
        return messageService.getPrivateHistory(targetId);
    }

    @GetMapping("/history/group")
    public List<MessageVO> getGroupHistory(@RequestParam Long targetId) {
        return messageService.getGroupHistory(targetId);
    }
}