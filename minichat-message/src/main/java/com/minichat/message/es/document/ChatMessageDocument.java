package com.minichat.message.es.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "chat_message")  // ES 索引名称
public class ChatMessageDocument { // 需要和ChatMessage保持一致

    @Id
    private Long id;  // 与 MySQL 的 id 保持一致

    @Field(type = FieldType.Long)
    private Long conversationId;

    @Field(type = FieldType.Integer)
    private Integer chatType;

    @Field(type = FieldType.Long)
    private Long fromId;

    @Field(type = FieldType.Long)
    private Long toId;

    @Field(type = FieldType.Integer)
    private Integer messageType;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Long)
    private Long clientSendTime;  // 用于时间排序

    /**
     * content 字段：全文检索的核心
     * - type = Text：可分词，用于全文搜索
     * - analyzer = ik_max_word：索引时细粒度分词
     * - search_analyzer = ik_smart：搜索时粗粒度分词
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;
}