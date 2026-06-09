package com.minichat.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minichat.dto.content.ImageContent;
import com.minichat.dto.content.VideoContent;
import com.minichat.dto.content.VoiceContent;
import com.minichat.dto.AckMessage;
import com.minichat.dto.SendMessageReq;
import com.minichat.dto.WsMessage;
import com.minichat.entity.GroupMember;
import com.minichat.entity.Message;
import com.minichat.mapper.GroupMemberMapper;
import com.minichat.mapper.MessageMapper;
import com.minichat.dto.content.TextContent;
import com.minichat.service.MessageService;
import com.minichat.vo.MessageVO;
import com.minichat.websocket.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.IdUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final GroupMemberMapper groupMemberMapper;

    private final SessionManager sessionManager;

    @Override
    public List<MessageVO> getAllHistory() {

        return messageMapper.selectList(
                        new QueryWrapper<Message>()
                                .orderByAsc("create_time")
                )
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public List<MessageVO> getPrivateHistory(Long targetId) {

        Long userId = 123L;

        return messageMapper.selectList(
                        new QueryWrapper<Message>()
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
    public List<MessageVO> getGroupHistory(Long groupId) {

        return messageMapper.selectList(
                    new QueryWrapper<Message>()
                            .eq("chat_type", 2)
                            .eq("to_id", groupId)
                            .orderByAsc("create_time")
                )
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    @Transactional
    @Override
    public Long sendMessage(SendMessageReq req, Long fromId) {

        // 构建消息
        Message message = buildMessage(req, fromId);
        Integer chatType = req.getChatType();

        // 消息入库并返回ACK
        messageMapper.insert(message);

        AckMessage ack = new AckMessage();
        ack.setMessageId(message.getId());

        WsMessage wsMessage = new WsMessage();
        wsMessage.setType(2);
        wsMessage.setData(
                JSONUtil.parseObj(ack)
        );

        sessionManager.sendToUser(
                message.getFromId(),
                JSONUtil.toJsonStr(wsMessage)
        );

        // 私发 or 群发
        if (chatType == 1) {
            sendPrivateMessage(message);
        }
        else if (chatType == 2) {
            sendGroupMessage(message);
        }
        else {
            throw new RuntimeException("请选择私发或群发");
        }

        return message.getId();
    }

    private Message buildMessage(SendMessageReq req, Long fromId) {

        Message message = new Message();

        BeanUtils.copyProperties(req, message);

        message.setId(IdUtil.getSnowflakeNextId());
        message.setFromId(fromId);
        message.setStatus(1);
        message.setCreateTime(LocalDateTime.now());

        switch (req.getMessageType()) {

            case 1 -> {

                TextContent content = new TextContent();
                content.setText(req.getContent());

                message.setContent(JSONUtil.toJsonStr(content));
            }

            case 2 -> {

                ImageContent content = new ImageContent();
                content.setUrl(req.getContent());

                message.setContent(JSONUtil.toJsonStr(content));
            }

            case 3 -> {

                VideoContent content = new VideoContent();
                content.setUrl(req.getContent());

                message.setContent(JSONUtil.toJsonStr(content));
            }

            case 4 -> {

                VoiceContent content = new VoiceContent();
                content.setUrl(req.getContent());

                message.setContent(JSONUtil.toJsonStr(content));
            }

            default -> throw new RuntimeException("暂不支持的消息类型");
        }

        return message;
    }

    private void sendPrivateMessage(Message message) {

        // 根据在线情况选择推送
        Long toId = message.getToId();
        boolean isOnline = sessionManager.isOnline(toId);

        System.out.println("toId=" + toId + ", online=" + isOnline);

        if (isOnline) {

            MessageVO vo = convertToVO(message);
            String payload = JSONUtil.toJsonStr(vo);

            sessionManager.sendToUser(
                    toId,
                    payload
            );
        }
    }

    private void sendGroupMessage(Message message) {

        // 查询群成员
        List<Long> memberIds = groupMemberMapper.selectList(
                                Wrappers.<GroupMember>lambdaQuery()
                                        .eq(GroupMember::getGroupId, message.getToId())
                        )
                        .stream()
                        .map(GroupMember::getUserId)
                        .filter(
                                id -> !id.equals(
                                        message.getFromId()
                                )
                        )
                        .toList();

        // 根据在线情况选择推送
        MessageVO vo = convertToVO(message);

        String payload = JSONUtil.toJsonStr(vo);

        for (Long userId : memberIds) {

            if (sessionManager.isOnline(userId)) {

                sessionManager.sendToUser(
                        userId,
                        payload
                );
            }
        }
    }

    private MessageVO convertToVO(Message message) {

        MessageVO vo = new MessageVO();

        vo.setId(message.getId());
        vo.setFromId(message.getFromId());
        vo.setToId(message.getToId());
        vo.setMessageType(message.getMessageType());

        vo.setContent(
                JSONUtil.parseObj(message.getContent())
        );

        vo.setCreateTime(message.getCreateTime());

        return vo;
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


