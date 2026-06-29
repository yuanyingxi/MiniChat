package com.minichat.message.es.repository;


import com.minichat.message.es.document.ChatMessageDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends ElasticsearchRepository<ChatMessageDocument, Long> {

    // ========== 场景1：按 conversationId 过滤 ==========
    // 排序通过 Pageable 动态传入，方法名中不带 OrderBy
    Page<ChatMessageDocument> findByConversationId(Long conversationId, Pageable pageable);

    // ========== 场景2：按 fromId 和 toId 过滤（单聊） ==========
    @Query("{\"bool\": {\"filter\": [{\"term\": {\"fromId\": \"?0\"}}, {\"term\": {\"toId\": \"?1\"}}]}}")
    Page<ChatMessageDocument> findByFromIdAndToId(Long fromId, Long toId, Pageable pageable);

    // ========== 场景3：conversationId + 内容搜索 ==========
    @Query("{\"bool\": {\"filter\": [{\"term\": {\"conversationId\": \"?0\"}}], \"must\": [{\"match\": {\"content\": \"?1\"}}]}}")
    Page<ChatMessageDocument> searchByConversationIdAndContent(Long conversationId, String keyword, Pageable pageable);

    // ========== 场景4：fromId + toId + 内容搜索 ==========
    @Query("{\"bool\": {\"filter\": [{\"term\": {\"fromId\": \"?0\"}}, {\"term\": {\"toId\": \"?1\"}}], \"must\": [{\"match\": {\"content\": \"?2\"}}]}}")
    Page<ChatMessageDocument> searchByFromIdAndToIdAndContent(Long fromId, Long toId, String keyword, Pageable pageable);
}