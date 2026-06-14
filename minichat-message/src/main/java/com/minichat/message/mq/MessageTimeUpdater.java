package com.minichat.message.mq;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.minichat.message.dto.HistoryReadEvent;
import com.minichat.message.entity.ChatMessage;
import com.minichat.message.mapper.MessageMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RocketMQMessageListener(
        topic = "history-read",
        consumerGroup = "history-group"
)
public class MessageTimeUpdater implements RocketMQListener<HistoryReadEvent> {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void onMessage(HistoryReadEvent event) {
        // 构建锁的唯一 key
        String lockKey = String.format("time-updater:lock:%d:%d:%d",
                event.getUserId(),
                event.getChatType(),
                event.getTargetId() == null ? 0 : event.getTargetId());
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 等待0秒，锁自动释放时间5秒（update 操作极快）
            if (lock.tryLock(0, 5, TimeUnit.SECONDS)) {
                System.out.println("更新查看时间，事件：" + event);
                executeUpdate(event);
            } else {
                System.out.println("正在被其他实例处理，跳过：" + event);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("获取锁被中断，事件：" + event);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void executeUpdate(HistoryReadEvent event) {
        Integer chatType = event.getChatType();

        if (chatType == 1) {
            messageMapper.update(
                    null,
                    new UpdateWrapper<ChatMessage>()
                            .set("update_time", LocalDateTime.now())
                            .nested(w -> w
                                    .eq("from_id", event.getUserId())
                                    .eq("to_id", event.getTargetId())
                                    .or()
                                    .eq("from_id", event.getTargetId())
                                    .eq("to_id", event.getUserId())
                            )
            );
        } else if (chatType == 2) {
            messageMapper.update(
                    null,
                    new UpdateWrapper<ChatMessage>()
                            .set("update_time", LocalDateTime.now())
                            .eq("chat_type", 2)
                            .eq("to_id", event.getTargetId())
            );
        } else if (chatType == 3) {
            messageMapper.update(
                    null,
                    new UpdateWrapper<ChatMessage>()
                            .set("update_time", LocalDateTime.now())
            );
        }
    }
}
