package com.minichat.message.mq;

import com.minichat.message.es.document.ChatMessageDocument;
import com.minichat.message.es.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RocketMQMessageListener(
    topic = "chat-sync-topic",               // 监听的 Topic，必须和生产者一致
    consumerGroup = "chat-sync-consumer-group" // 消费者组（同一个组内，消息只会被一台机器消费）
)
public class ChatMessageSyncConsumer implements RocketMQListener<ChatMessageSyncEvent> {

    @Autowired
    private ChatMessageRepository chatMessageRepository; // ES 操作接口

    @Override
    public void onMessage(ChatMessageSyncEvent event) {
        log.info("消费者收到消息, ID: {}, 操作: {}", event.getId(), event.getOperation());

        try {
            // 根据操作类型执行不同的 ES 操作
            if ("INSERT".equals(event.getOperation()) || "UPDATE".equals(event.getOperation())) {
                // 转换为 ES 文档实体
                ChatMessageDocument doc = getChatMessageDocument(event);

                // 保存到 ES
                chatMessageRepository.save(doc);
                log.info("ES 写入成功, ID: {}", event.getId());

            } else if ("DELETE".equals(event.getOperation())) {
                // 删除 ES 文档
                chatMessageRepository.deleteById(event.getId());
                log.info("ES 删除成功, ID: {}", event.getId());
            }
        } catch (Exception e) {
            log.error("ES 操作失败, ID: {}, 错误: {}", event.getId(), e.getMessage());
            // 【关键】抛出异常，让 RocketMQ 框架进行重试（默认重试 16 次）
            // 如果重试 16 次都失败，消息会进入死信队列，需要人工介入
            throw new RuntimeException("ES 同步失败", e);
        }
    }

    private static @NonNull ChatMessageDocument getChatMessageDocument(ChatMessageSyncEvent event) {
        ChatMessageDocument doc = new ChatMessageDocument();
        doc.setId(event.getId());
        doc.setConversationId(event.getConversationId());
        doc.setFromId(event.getFromId());
        doc.setToId(event.getToId());
        doc.setChatType(event.getChatType());
        doc.setMessageType(event.getMessageType());
        doc.setStatus(event.getStatus());
        doc.setClientSendTime(event.getClientSendTime());
        doc.setContent(event.getContent());
        doc.setCreateTime(LocalDateTime.parse(event.getCreateTime()));
        doc.setUpdateTime(LocalDateTime.parse(event.getUpdateTime()));
        return doc;
    }
}