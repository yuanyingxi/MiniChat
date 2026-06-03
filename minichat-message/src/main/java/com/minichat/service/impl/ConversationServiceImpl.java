package com.minichat.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minichat.entity.Conversation;
import com.minichat.mapper.ConversationMapper;
import com.minichat.service.ConversationService;
import com.minichat.vo.ConversationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationMapper conversationMapper;

    @Override
    public List<ConversationVO> list() {

        List<Conversation> conversations =
                conversationMapper.selectList(null);

        return conversations.stream()
                .map(this::convertToVO)
                .toList();
    }

    private ConversationVO convertToVO(
            Conversation conversation
    ) {

        ConversationVO vo =
                new ConversationVO();

        vo.setId(
                conversation.getId()
        );

        vo.setConversationType(
                conversation.getConversationType()
        );

        vo.setTargetId(
                conversation.getTargetId()
        );

        if (conversation.getLastMessageContent() != null) {

            vo.setLastMessageContent(
                    JSONUtil.parseObj(
                            conversation.getLastMessageContent()
                    )
            );
        }

        vo.setLastMessageTime(
                conversation.getLastMessageTime()
        );

        return vo;
    }
}
