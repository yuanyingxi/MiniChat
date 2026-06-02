package com.minichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minichat.dto.SendMessageReq;
import com.minichat.entity.Message;
import com.minichat.mapper.MessageMapper;
import com.minichat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.IdUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public Long sendMessage(SendMessageReq req) {

        Message message = new Message();

        BeanUtils.copyProperties(req, message);

        message.setMessageId(IdUtil.getSnowflakeNextId());

        message.setStatus(1);

        message.setCreateTime(LocalDateTime.now());

        // 后面从JWT获取
        message.setFromId(1001L);

        messageMapper.insert(message);

        return message.getMessageId();
    }

    @Override
    public List<Message> getHistory(Long userId) {

        Long currentUserId = 1001L;

        return messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .and(w -> w
                                .eq(Message::getFromId, currentUserId)
                                .eq(Message::getToId, userId)
                                .or()
                                .eq(Message::getFromId, userId)
                                .eq(Message::getToId, currentUserId)
                        )
                        .orderByAsc(Message::getCreateTime)
        );
    }
}


