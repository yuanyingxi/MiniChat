package com.minichat.message.mq;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.minichat.message.dto.HistoryReadEvent;
import com.minichat.message.entity.ChatMessage;
import com.minichat.message.mapper.MessageMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RocketMQMessageListener(
        topic = "history-read",
        consumerGroup = "history-group"
)
public class MessageTimeUpdater implements RocketMQListener<HistoryReadEvent> {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public void onMessage(HistoryReadEvent event) {

        System.out.println("更新查看时间");

        Integer chatType = event.getChatType();

        if(chatType == 1){
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
        }
        else if(chatType == 2){
            messageMapper.update(
                    null,
                    new UpdateWrapper<ChatMessage>()
                            .set("update_time", LocalDateTime.now())
                            .eq("chat_type", 2)
                            .eq("to_id", event.getTargetId())
            );
        }
        else if(chatType == 3){
            messageMapper.update(
                    null,
                    new UpdateWrapper<ChatMessage>()
                            .set("update_time", LocalDateTime.now())
            );
        }
        else{

        }
    }
}
