package com.minichat.message.mq;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import com.minichat.message.dto.MessageVO;
import com.minichat.message.dto.WsMessage;
import com.minichat.message.entity.ChatMessage;
import com.minichat.message.websocket.SessionManager;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RocketMQMessageListener(
        topic = "chat-message",
        consumerGroup = "push-group"
)
public class MessagePusher implements RocketMQListener<ChatMessage> {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void onMessage(ChatMessage message) {
        // 分布式锁防止重复消费
        // 使用消息ID作为分布式锁的key
        String lockKey = "msg:push:" + message.getId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试加锁，waitTime=0（不等待，立即失败），leaseTime=10秒（自动释放）
            if (lock.tryLock(0, 10, TimeUnit.SECONDS)) {

                System.out.println("收到消息，开始推送，消息ID：" + message.getId());
                // 私发 or 群发
                if (message.getChatType() == 1) {
                    sendPrivateMessage(message);
                } else if (message.getChatType() == 2) {
                    sendGroupMessage(message);
                } else {
                    throw new RuntimeException("消息类型并非私聊或群聊！");
                }

            } else {
                // 获取锁失败，说明有其他实例正在处理该消息
                System.out.println("消息 " + message.getId() + " 已被其他实例处理，当前实例跳过");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("获取锁被中断，消息ID：" + message.getId());
        } finally {
            // 释放锁（仅当当前线程持有锁时）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void sendPrivateMessage(ChatMessage chatMessage) {

        Long toId = chatMessage.getToId();
        MessageVO vo = convertToVO(chatMessage);

        // 包装为 WsFrame: type=0 表示收到新消息
        WsMessage frame = new WsMessage();
        frame.setType(0);
        frame.setData(JSONUtil.parseObj(vo));
        String payload = JSONUtil.toJsonStr(frame);

        sessionManager.sendToUser(
                toId,
                payload
        );
    }

    private void sendGroupMessage(ChatMessage chatMessage) {
        String url = "http://minichat-user/group/" + chatMessage.getToId() + "/members";

        String responseBody = restTemplate.getForObject(url, String.class);
        if (responseBody == null) {
            System.out.println("获取群成员列表失败 - responseBody");
            return;
        }

        // 用 Hutool 解析 JSON
        JSONObject json = JSONUtil.parseObj(responseBody);
        if (json.getInt("code") != 200) {
            System.out.println("获取群成员列表失败 - json");
            return;
        }

        // 提取成员ID列表
        JSONArray dataArray = json.getJSONArray("data");
        List<Long> memberIds = dataArray.stream()
                .map(obj -> ((JSONObject) obj).getLong("userId"))
                .filter(id -> !id.equals(chatMessage.getFromId()))
                .toList();

        // 推送消息
        MessageVO vo = convertToVO(chatMessage);

        // 包装为 WsFrame: type=0 表示收到新消息
        WsMessage frame = new WsMessage();
        frame.setType(0);
        frame.setData(JSONUtil.parseObj(vo));
        String payload = JSONUtil.toJsonStr(frame);

        for (Long userId : memberIds) {
            sessionManager.sendToUser(userId, payload);
        }
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
}
