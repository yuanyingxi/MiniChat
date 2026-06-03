package com.minichat.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minichat.constant.RedisKeyConstant;
import com.minichat.dto.SendMessageReq;
import com.minichat.entity.Conversation;
import com.minichat.entity.GroupMember;
import com.minichat.entity.Message;
import com.minichat.mapper.ConversationMapper;
import com.minichat.mapper.GroupMemberMapper;
import com.minichat.mapper.MessageMapper;
import com.minichat.model.content.TextContent;
import com.minichat.service.MessageService;
import com.minichat.vo.MessageVO;
import com.minichat.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.IdUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final ConversationMapper conversationMapper;
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final GroupMemberMapper groupMemberMapper;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    @Override
    public Long sendMessage(SendMessageReq req) {

        // 1. 构建消息（纯数据构造）
        Message message = buildMessage(req);

        // 2. 根据类型分发
        if (req.getChatType() == 1) {
            sendPrivateMessage(message);
        } else if (req.getChatType() == 2) {
            sendGroupMessage(message);
        } else {
            throw new RuntimeException("不支持的聊天类型");
        }

        return message.getId();
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

    private Conversation getOrCreateConversation(Long fromId, Long toId) {

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

            conversation.setId(
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

    private Conversation getOrCreateGroupConversation(Long groupId) {

        Conversation conversation = conversationMapper.selectOne(
                Wrappers.<Conversation>lambdaQuery()
                        .eq(Conversation::getConversationType, 2)
                        .eq(Conversation::getTargetId, groupId)
        );

        if (conversation != null) {
            return conversation;
        }

        conversation = new Conversation();
        conversation.setId(IdUtil.getSnowflakeNextId());
        conversation.setConversationType(2); // 群聊
        conversation.setTargetId(groupId);

        conversationMapper.insert(conversation);

        return conversation;
    }

    private void updateConversation(Conversation conversation, Message message) {

        conversation.setLastMessageId(
                message.getId()
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

    private Message buildMessage(SendMessageReq req) {

        Message message = new Message();

        BeanUtils.copyProperties(req, message);

        // 消息ID
        message.setId(IdUtil.getSnowflakeNextId());

        // 默认状态
        message.setStatus(1); // 正常（已发送）

        // 服务端时间
        message.setCreateTime(LocalDateTime.now());

        // TODO：从 JWT 获取真实用户
        message.setFromId(1001L);

        // 消息内容序列化
        if (req.getMessageType() == 1) {

            TextContent textContent = new TextContent();
            textContent.setText(req.getContent());

            message.setContent(JSONUtil.toJsonStr(textContent));

        } else {
            throw new RuntimeException("暂不支持的消息类型");
        }

        return message;
    }

    private void sendPrivateMessage(Message message) {

        // 获取会话（单聊）
        Conversation conversation = getOrCreateConversation(
                message.getFromId(),
                message.getToId()
        );

        message.setConversationId(conversation.getId());

        // 保存消息
        messageMapper.insert(message);

        // 更新会话
        updateConversation(conversation, message);

        // 判断在线
        boolean online = chatWebSocketHandler.isOnline(message.getToId());

        System.out.println("online=" + online + ", userId=" + message.getToId());

        MessageVO vo = convertToVO(message);
        String payload = JSONUtil.toJsonStr(vo);

        if (online) {

            // 在线直接推送
            chatWebSocketHandler.sendToUser(
                    message.getToId(),
                    payload
            );

        } else {

            // ❌ 离线 → 写 Redis
            String key = RedisKeyConstant.OFFLINE_MESSAGE + message.getToId();

            redisTemplate.opsForList().leftPush(key, payload);
        }
    }

    private void sendGroupMessage(Message message) {

        Long groupId = message.getToId();

        // 1. 校验是否为群成员
        GroupMember self = groupMemberMapper.selectOne(
                Wrappers.<GroupMember>lambdaQuery()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, message.getFromId())
        );

        if (self == null) {
            throw new RuntimeException("你不是群成员");
        }

        // 2. 获取或创建群会话
        Conversation conversation = getOrCreateGroupConversation(groupId);

        message.setConversationId(conversation.getId());

        // 3. 保存消息
        messageMapper.insert(message);

        // 4. 更新会话
        updateConversation(conversation, message);

        // 5. 查询群成员
        List<GroupMember> members = groupMemberMapper.selectList(
                Wrappers.<GroupMember>lambdaQuery()
                        .eq(GroupMember::getGroupId, groupId)
        );

        // 6. 广播消息
        MessageVO vo = convertToVO(message);
        String payload = JSONUtil.toJsonStr(vo);

        for (GroupMember member : members) {

            // 不发给自己
            if (member.getUserId().equals(message.getFromId())) {
                continue;
            }

            chatWebSocketHandler.sendToUser(
                    member.getUserId(),
                    payload
            );
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
}


