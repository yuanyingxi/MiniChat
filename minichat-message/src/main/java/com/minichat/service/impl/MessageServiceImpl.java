package com.minichat.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minichat.dto.SendMessageReq;
import com.minichat.entity.Conversation;
import com.minichat.entity.Message;
import com.minichat.mapper.ConversationMapper;
import com.minichat.mapper.MessageMapper;
import com.minichat.model.content.TextContent;
import com.minichat.service.MessageService;
import com.minichat.vo.MessageVO;
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
    private final ConversationMapper conversationMapper;

    @Override
    public Long sendMessage(SendMessageReq req) {

        Message message = new Message();

        BeanUtils.copyProperties(req, message);

        // 消息ID
        message.setMessageId(IdUtil.getSnowflakeNextId());

        // 默认正常状态
        message.setStatus(1);

        // 服务端时间
        message.setCreateTime(LocalDateTime.now());

        // TODO 后续从 JWT 获取
        message.setFromId(1001L);

        // 获取或创建会话
        Conversation conversation = getOrCreateConversation(
                message.getFromId(),
                message.getToId()
        );

        message.setConversationId(
                conversation.getConversationId()
        );

        // ==========================
        // 构造消息内容 JSON
        // ==========================

        if (req.getMessageType() == 1) {

            TextContent textContent = new TextContent();

            textContent.setText(req.getContent());

            message.setContent(
                    JSONUtil.toJsonStr(textContent)
            );

        } else {

            throw new RuntimeException("暂不支持的消息类型");
        }

        // 保存消息
        messageMapper.insert(message);

        // 更新会话最后消息
        updateConversation(conversation, message);

        return message.getMessageId();
    }

    @Override
    public List<MessageVO> getHistory(Long conversationId) {

        List<Message> messages =
                messageMapper.selectList(
                        Wrappers.<Message>lambdaQuery()
                                .eq(
                                        Message::getConversationId,
                                        conversationId
                                )
                                .orderByAsc(
                                        Message::getCreateTime
                                )
                );

        return messages.stream()
                .map(this::convertToVO)
                .toList();
    }

    private Conversation getOrCreateConversation(
            Long fromId,
            Long toId
    ) {

        Long minId = Math.min(fromId, toId);

        Long maxId = Math.max(fromId, toId);

        Long conversationId =
                Long.valueOf(minId + "" + maxId);

        Conversation conversation =
                conversationMapper.selectById(
                        conversationId
                );

        if (conversation == null) {

            conversation = new Conversation();

            conversation.setConversationId(
                    conversationId
            );

            conversation.setConversationType(1);

            conversation.setTargetId(toId);

            conversationMapper.insert(
                    conversation
            );
        }

        return conversation;
    }

    private void updateConversation(
            Conversation conversation,
            Message message
    ) {

        conversation.setLastMessageId(
                message.getMessageId()
        );

        conversation.setLastMessageContent(
                message.getContent()
        );

        conversation.setLastMessageTime(
                message.getCreateTime()
        );

        conversationMapper.updateById(
                conversation
        );
    }

    private MessageVO convertToVO(Message message) {

        MessageVO vo = new MessageVO();

        vo.setMessageId(message.getMessageId());
        vo.setFromId(message.getFromId());
        vo.setToId(message.getToId());
        vo.setMessageType(message.getMessageType());

        vo.setContent(
                JSONUtil.parseObj(message.getContent())
        );

        vo.setCreateTime(message.getCreateTime());

        return vo;
    }
}


