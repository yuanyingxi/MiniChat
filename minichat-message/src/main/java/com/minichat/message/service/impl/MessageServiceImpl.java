package com.minichat.message.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.minichat.message.dto.*;
import com.minichat.message.dto.content.ImageContent;
import com.minichat.message.dto.content.VideoContent;
import com.minichat.message.dto.content.VoiceContent;
import com.minichat.message.entity.ChatMessage;
import com.minichat.message.mapper.MessageMapper;
import com.minichat.message.dto.content.TextContent;
import com.minichat.message.service.MessageService;
import com.minichat.message.websocket.SessionManager;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.IdUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final RocketMQTemplate rocketMQTemplate;

    private final MessageMapper messageMapper;

    private final SessionManager sessionManager;

    @Override
    public List<MessageVO> getAllHistory() {

        rocketMQTemplate.convertAndSend(
                "history-read",
                new HistoryReadEvent(
                        3,
                        null,
                        null
                )
        );

        return messageMapper.selectList(
                        new QueryWrapper<ChatMessage>()
                                .orderByAsc("create_time")
                )
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public List<MessageVO> getPrivateHistory(Long userId, Long targetId) {

        rocketMQTemplate.convertAndSend(
                "history-read",
                new HistoryReadEvent(
                        1,
                        userId,
                        targetId
                )
        );

        return messageMapper.selectList(
                        new QueryWrapper<ChatMessage>()
                                .nested(w -> w
                                        .eq("from_id", userId)
                                        .eq("to_id", targetId)
                                        .or()
                                        .eq("from_id", targetId)
                                        .eq("to_id", userId)
                                )
                                .orderByAsc("create_time")
                )
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public List<MessageVO> getGroupHistory(Long userId, Long groupId) {

        rocketMQTemplate.convertAndSend(
                "history-read",
                new HistoryReadEvent(
                        2,
                        userId,
                        groupId
                )
        );

        return messageMapper.selectList(
                    new QueryWrapper<ChatMessage>()
                            .eq("chat_type", 2)
                            .eq("to_id", groupId)
                            .orderByAsc("create_time")
                )
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    private MessageVO convertToVO(ChatMessage chatMessage) {

        MessageVO vo = new MessageVO();

        vo.setId(chatMessage.getId());
        vo.setFromId(chatMessage.getFromId());
        vo.setToId(chatMessage.getToId());
        vo.setMessageType(chatMessage.getMessageType());

        vo.setContent(
                JSONUtil.parseObj(chatMessage.getContent())
        );

        vo.setCreateTime(chatMessage.getCreateTime());

        return vo;
    }

    @Transactional
    @Override
    public Long sendMessage(SendMessageReq req, Long fromId) {

        // 构建消息
        ChatMessage chatMessage = buildMessage(req, fromId);

        // 消息入库并返回ACK
        messageMapper.insert(chatMessage);

        AckMessage ack = new AckMessage();
        ack.setMessageId(chatMessage.getId());

        WsMessage wsMessage = new WsMessage();
        wsMessage.setType(2);
        wsMessage.setData(
                JSONUtil.parseObj(ack)
        );

        sessionManager.sendToUser(
                chatMessage.getFromId(),
                JSONUtil.toJsonStr(wsMessage)
        );

        //
        rocketMQTemplate.convertAndSend(
                "chat-message",
                chatMessage
        );

        return chatMessage.getId();
    }

    private ChatMessage buildMessage(SendMessageReq req, Long fromId) {

        ChatMessage chatMessage = new ChatMessage();

        BeanUtils.copyProperties(req, chatMessage);

        chatMessage.setId(IdUtil.getSnowflakeNextId());
        chatMessage.setFromId(fromId);
        chatMessage.setStatus(1);
        chatMessage.setCreateTime(LocalDateTime.now());
        chatMessage.setUpdateTime(LocalDateTime.now());

        switch (req.getMessageType()) {

            case 1 -> {

                TextContent content = new TextContent();
                content.setText(req.getContent());

                chatMessage.setContent(JSONUtil.toJsonStr(content));
            }

            case 2 -> {

                ImageContent content = new ImageContent();
                content.setUrl(req.getContent());

                chatMessage.setContent(JSONUtil.toJsonStr(content));
            }

            case 3 -> {

                VideoContent content = new VideoContent();
                content.setUrl(req.getContent());

                chatMessage.setContent(JSONUtil.toJsonStr(content));
            }

            case 4 -> {

                VoiceContent content = new VoiceContent();
                content.setUrl(req.getContent());

                chatMessage.setContent(JSONUtil.toJsonStr(content));
            }

            default -> throw new RuntimeException("暂不支持的消息类型");
        }

        return chatMessage;
    }







//    private Conversation getOrCreateConversation(Long fromId, Integer conversationType, Long toId) {
//
//        Conversation conversation =
//                conversationMapper.selectOne(
//                        Wrappers.<Conversation>lambdaQuery()
//                                .eq(Conversation::getUserId, fromId)
//                                .eq(Conversation::getConversationType, conversationType)
//                                .eq(Conversation::getTargetId, toId)
//                );
//
//        if (conversation == null) {
//
//            conversation = new Conversation();
//
//            conversation.setId(IdUtil.getSnowflakeNextId());
//            conversation.setConversationType(conversationType);
//            conversation.setUserId(fromId);
//            conversation.setTargetId(toId);
//
//            conversationMapper.insert(
//                    conversation
//            );
//        }
//
//        return conversation;
//    }
//    private void updateConversation(Conversation conversation, Message message) {
//
//        conversation.setLastMessageId(
//                message.getId()
//        );
//
//        conversation.setLastMessageContent(
//                message.getContent()
//        );
//
//        conversation.setLastMessageTime(
//                message.getCreateTime()
//        );
//
//        conversationMapper.updateById(
//                conversation
//        );
//    }
}


