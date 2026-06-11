package com.minichat.message;

import com.minichat.message.entity.ChatMessage;
import com.minichat.message.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class ChatMessageMapperTest {

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testInsert() {

        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setId(2L);
        chatMessage.setChatType(1);
        chatMessage.setFromId(1001L);
        chatMessage.setToId(1002L);
        chatMessage.setMessageType(1);
        chatMessage.setContent("{\"text\":\"hello\"}");

        int rows = messageMapper.insert(chatMessage);

        System.out.println("插入行数：" + rows);
    }

    @Test
    public void testSelectById() {

        ChatMessage chatMessage = messageMapper.selectById(2L);

        System.out.println(chatMessage);
    }

    @Test
    public void testUpdateById() {

        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setId(2L);
        chatMessage.setContent("{\"text\":\"hello update\"}");

        int rows = messageMapper.updateById(chatMessage);

        System.out.println("更新行数：" + rows);
    }

    @Test
    public void testDeleteById() {

        int rows = messageMapper.deleteById(2L);

        System.out.println("删除行数：" + rows);
    }
}