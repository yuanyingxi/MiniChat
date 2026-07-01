package com.minichat.message.service;

import com.minichat.message.es.document.ChatMessageDocument;
import com.minichat.message.es.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSearchService {

    private final ChatMessageRepository repository;

    /**
     * 统一排序构建器
     * @param sortField 排序字段：支持 "createTime" 或 "clientSendTime"，默认 createTime
     * @param direction 排序方向：asc 或 desc，默认 desc
     */
    private Sort buildSort(String sortField, String direction) {
        if (sortField == null || sortField.isEmpty()) {
            sortField = "createTime";
        }
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, sortField);
    }

    // ===== 场景1：按会话查询 =====

    /**
     * 按会话查询
     * @param conversationId 会话id
     * @param sortField 排序字段：支持 "createTime" 或 "clientSendTime"
     * @param direction 排序方向：asc 或 desc
     * @param page 分页页码
     * @param size 分页的每页大小
     * @return Page<ChatMessageDocument>
     */
    public Page<ChatMessageDocument> queryByConversation(Long conversationId, String sortField, String direction, int page, int size) {
        Sort sort = buildSort(sortField, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findByConversationId(conversationId, pageable);
    }

    // ===== 场景2：按单聊双方查询 =====

    /**
     * 按单聊双方查询
     * @param fromId 发送方id
     * @param toId 接收方id
     * @param sortField 排序字段：支持 "createTime" 或 "clientSendTime"
     * @param direction 排序方向：asc 或 desc
     * @param page 分页页码
     * @param size 分页的每页大小
     * @return Page<ChatMessageDocument>
     */
    public Page<ChatMessageDocument> queryByUsers(Long fromId, Long toId, String sortField, String direction, int page, int size) {
        Sort sort = buildSort(sortField, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findByFromIdAndToId(fromId, toId, pageable);
    }

    // ===== 场景3：会话内关键词搜索 =====

    /**
     * 会话内关键词搜索
     * @param conversationId 会话id
     * @param keyword 关键词
     * @param sortField 排序字段：支持 "createTime" 或 "clientSendTime"
     * @param direction 排序方向：asc 或 desc
     * @param page 分页页码
     * @param size 分页的每页大小
     * @return Page<ChatMessageDocument>
     */
    public Page<ChatMessageDocument> searchInConversation(Long conversationId, String keyword, String sortField, String direction, int page, int size) {
        Sort sort = buildSort(sortField, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.searchByConversationIdAndContent(conversationId, keyword, pageable);
    }

    // ===== 场景4：单聊双方间关键词搜索 =====

    /**
     * 单聊双方间关键词搜索
     * @param fromId 发送方id
     * @param toId 接收方id
     * @param keyword 关键词
     * @param sortField 排序字段：支持 "createTime" 或 "clientSendTime"
     * @param direction 排序方向：asc 或 desc
     * @param page 分页页码
     * @param size 分页的每页大小
     * @return Page<ChatMessageDocument>
     */
    public Page<ChatMessageDocument> searchBetweenUsers(Long fromId, Long toId, String keyword, String sortField, String direction, int page, int size) {
        Sort sort = buildSort(sortField, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.searchByFromIdAndToIdAndContent(fromId, toId, keyword, pageable);
    }
}