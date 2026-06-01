package com.minichat;

import com.minichat.entity.Message;
import com.minichat.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class MessageMapperTest {

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testInsert() {

        Message message = new Message();

        message.setMessageId(2L);
        message.setChatType(1);
        message.setFromId(1001L);
        message.setToId(1002L);
        message.setMessageType(1);
        message.setContent("{\"text\":\"hello\"}");

        int rows = messageMapper.insert(message);

        System.out.println("插入行数：" + rows);
    }

    @Test
    public void testSelectById() {

        Message message = messageMapper.selectById(2L);

        System.out.println(message);
    }

    @Test
    public void testUpdateById() {

        Message message = new Message();

        message.setMessageId(2L);
        message.setContent("{\"text\":\"hello update\"}");

        int rows = messageMapper.updateById(message);

        System.out.println("更新行数：" + rows);
    }

    @Test
    public void testDeleteById() {

        int rows = messageMapper.deleteById(2L);

        System.out.println("删除行数：" + rows);
    }
}