package com.minichat.message.controller;

import com.minichat.message.dto.MessageVO;
import com.minichat.message.es.document.ChatMessageDocument;
import com.minichat.message.service.ChatSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatSearchController {

    private final ChatSearchService chatSearchService;

    /**
     * 统一的聊天记录查询接口
     * 支持按 conversationId 或 (fromId + toId) 过滤
     * 支持关键词搜索（content 字段）
     * 支持动态排序（createTime 或 clientSendTime）
     * 支持分页
     * <p>
     * GET /api/chat/history/content?conversationId=100&keyword=hello&sortField=createTime&direction=desc&page=0&size=20
     */
    @GetMapping("/history/content")
    public Page<MessageVO> getHistory(
            @RequestParam(required = false) Long conversationId,
            @RequestParam(required = false) Long fromId,
            @RequestParam(required = false) Long toId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createTime") String sortField,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("查询请求: conversationId={}, fromId={}, toId={}, keyword={}, sortField={}, direction={}, page={}, size={}",
                conversationId, fromId, toId, keyword, sortField, direction, page, size);
        Page<ChatMessageDocument> docPage;
        // 1. 按会话查询（包括会话内关键词搜索）
        if (conversationId != null) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 会话内关键词搜索
                docPage = chatSearchService.searchInConversation(conversationId, keyword.trim(), sortField, direction, page, size);
            } else {
                // 会话时间线查询
                docPage = chatSearchService.queryByConversation(conversationId, sortField, direction, page, size);
            }
        }

        // 2. 按单聊双方查询（包括双方间关键词搜索）
        else if (fromId != null && toId != null) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 双方间关键词搜索
                docPage = chatSearchService.searchBetweenUsers(fromId, toId, keyword.trim(), sortField, direction, page, size);
            } else {
                // 双方时间线查询
                docPage = chatSearchService.queryByUsers(fromId, toId, sortField, direction, page, size);
            }
        }
        else{
            // 3. 参数不合法
            throw new IllegalArgumentException("必须提供 conversationId 或 (fromId + toId)");
        }

        return docPage.map(this::convertToVO);
    }


    /**
     * 文档 → DTO 转换函数（可复用）
     * 只提取前端需要的字段，隐藏 ES 内部结构
     */
    private MessageVO convertToVO(ChatMessageDocument doc) {
        if (doc == null) {
            return null;
        }
        MessageVO vo = new MessageVO();
        vo.setId(doc.getId());
        vo.setFromId(doc.getFromId());
        vo.setToId(doc.getToId());
        vo.setMessageType(doc.getMessageType());
        vo.setContent(doc.getContent());  // content 是 String，赋给 Object 类型没问题
        vo.setCreateTime(doc.getCreateTime());
        // 注意：如果 MessageVO 中有其他字段（如 conversationId），可以在这里补充设置
        // 但目前 MessageVO 只包含上述字段，所以无需其他映射
        return vo;
    }

}