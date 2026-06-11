package com.minichat.message.mq;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.minichat.message.dto.MessageVO;
import com.minichat.message.entity.ChatMessage;
import com.minichat.message.websocket.SessionManager;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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

    @Override
    public void onMessage(ChatMessage message) {

        System.out.println("收到消息，开始推送");

        // 私发 or 群发
        if (message.getChatType() == 1) {
            sendPrivateMessage(message);
        }
        else if (message.getChatType() == 2) {
            sendGroupMessage(message);
        }
        else {
            throw new RuntimeException("消息类型并非私聊或群聊！");
        }

    }

    private void sendPrivateMessage(ChatMessage chatMessage) {

        // 根据在线情况选择推送
        Long toId = chatMessage.getToId();
        boolean isOnline = sessionManager.isOnline(toId);

        System.out.println("toId=" + toId + ", online=" + isOnline);

        if (isOnline) {

            MessageVO vo = convertToVO(chatMessage);
            String payload = JSONUtil.toJsonStr(vo);

            sessionManager.sendToUser(
                    toId,
                    payload
            );
        }
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
        String payload = JSONUtil.toJsonStr(vo);

        for (Long userId : memberIds) {
            if (sessionManager.isOnline(userId)) {
                sessionManager.sendToUser(userId, payload);
            }
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
