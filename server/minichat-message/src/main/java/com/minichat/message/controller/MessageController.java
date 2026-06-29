package com.minichat.message.controller;

import com.minichat.common.result.Result;
import com.minichat.message.service.MessageService;
import com.minichat.message.dto.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    @Autowired
    private final MessageService messageService;

    @GetMapping("/history/all")
    public Result<List<MessageVO>> getAllHistory() {
        return Result.success(messageService.getAllHistory());
    }

    @GetMapping("/history/private")
    public Result<List<MessageVO>> getPrivateHistory(@RequestHeader("userId") Long userId,
                                                      @RequestParam("targetId") Long targetId,
                                                      @RequestParam(value = "startTime", required = false) LocalDateTime startTime,
                                                      @RequestParam(value = "endTime", required = false) LocalDateTime endTime) {
        return Result.success(messageService.getPrivateHistory(userId, targetId, startTime, endTime));
    }

    @GetMapping("/history/group")
    public Result<List<MessageVO>> getGroupHistory(@RequestHeader("userId") Long userId,
                                                    @RequestParam("targetId") Long targetId,
                                                    @RequestParam(value = "startTime", required = false) LocalDateTime startTime,
                                                    @RequestParam(value = "endTime", required = false) LocalDateTime endTime) {
        return Result.success(messageService.getGroupHistory(userId, targetId, startTime, endTime));
    }
}
