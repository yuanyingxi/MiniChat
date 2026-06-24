package com.minichat.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minichat.message.entity.ChatMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<ChatMessage> {
    /**
     * 批量插入（使用 @Insert 注解 + 动态 SQL）
     * @param list 消息列表
     * @return 插入的行数
     */
    @Insert({
            "<script>",
            "INSERT INTO chat_message (",
            "    id, conversation_id, chat_type, from_id, to_id,",
            "    message_type, status, client_send_time, content,",
            "    update_time, create_time",
            ") VALUES ",
            "<foreach collection='list' item='item' separator=','>",
            "    (",
            "        #{item.id}, #{item.conversationId}, #{item.chatType},",
            "        #{item.fromId}, #{item.toId}, #{item.messageType},",
            "        #{item.status}, #{item.clientSendTime}, #{item.content},",
            "        #{item.updateTime}, #{item.createTime}",
            "    )",
            "</foreach>",
            "</script>"
    })
    int insertBatch(@Param("list") List<ChatMessage> list);
}
