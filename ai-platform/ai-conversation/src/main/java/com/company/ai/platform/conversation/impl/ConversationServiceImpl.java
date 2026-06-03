package com.company.ai.platform.conversation.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.ai.common.core.exception.BizException;
import com.company.ai.common.core.result.ResultCode;
import com.company.ai.platform.conversation.ConversationService;
import com.company.ai.platform.conversation.entity.Conversation;
import com.company.ai.platform.conversation.entity.ConversationMessage;
import com.company.ai.platform.conversation.mapper.ConversationMapper;
import com.company.ai.platform.conversation.mapper.ConversationMessageMapper;
import com.company.ai.platform.conversation.model.ConversationMessageVO;
import com.company.ai.platform.conversation.model.ConversationVO;
import com.company.ai.platform.conversation.model.CreateConversationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationMapper conversationMapper;
    private final ConversationMessageMapper messageMapper;

    private static final int TITLE_MAX_LENGTH = 50;

    @Override
    public ConversationVO createConversation(String appId, CreateConversationRequest request) {
        Conversation conversation = new Conversation();
        conversation.setAppId(appId);
        conversation.setUserId(request.getUserId());
        conversation.setTitle(request.getTitle());
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationMapper.insert(conversation);
        return toConversationVO(conversation);
    }

    @Override
    public List<ConversationVO> listConversations(String appId, String userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getAppId, appId)
                .orderByDesc(Conversation::getUpdatedAt);
        if (userId != null && !userId.isBlank()) {
            wrapper.eq(Conversation::getUserId, userId);
        }
        List<Conversation> conversations = conversationMapper.selectList(wrapper);
        return conversations.stream().map(this::toConversationVO).toList();
    }

    @Override
    public ConversationVO getConversation(String appId, Long conversationId) {
        Conversation conversation = getAndValidate(appId, conversationId);
        return toConversationVO(conversation);
    }

    @Override
    public void deleteConversation(String appId, Long conversationId) {
        getAndValidate(appId, conversationId);
        conversationMapper.deleteById(conversationId);
    }

    @Override
    public List<ConversationMessageVO> getMessages(String appId, Long conversationId) {
        getAndValidate(appId, conversationId);
        List<ConversationMessage> messages = messageMapper.selectList(
                new LambdaQueryWrapper<ConversationMessage>()
                        .eq(ConversationMessage::getConversationId, conversationId)
                        .orderByAsc(ConversationMessage::getCreatedAt));
        return messages.stream().map(this::toMessageVO).toList();
    }

    @Override
    public ConversationMessage saveUserMessage(Long conversationId, String content) {
        ConversationMessage msg = new ConversationMessage();
        msg.setConversationId(conversationId);
        msg.setRole("user");
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(msg);

        // Update conversation's updatedAt
        updateConversationTimestamp(conversationId);
        return msg;
    }

    @Override
    public ConversationMessage saveAssistantMessage(Long conversationId, String content, Integer tokens) {
        ConversationMessage msg = new ConversationMessage();
        msg.setConversationId(conversationId);
        msg.setRole("assistant");
        msg.setContent(content);
        msg.setTokens(tokens);
        msg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(msg);

        updateConversationTimestamp(conversationId);
        return msg;
    }

    @Override
    @Transactional
    public Long ensureConversation(String appId, Long conversationId, String userId, String firstMessage) {
        if (conversationId != null) {
            getAndValidate(appId, conversationId);
            return conversationId;
        }
        Conversation conversation = new Conversation();
        conversation.setAppId(appId);
        conversation.setUserId(userId);
        conversation.setTitle(generateTitle(firstMessage));
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationMapper.insert(conversation);
        return conversation.getId();
    }

    private Conversation getAndValidate(String appId, Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getAppId().equals(appId)) {
            throw new BizException(ResultCode.CONVERSATION_NOT_FOUND);
        }
        return conversation;
    }

    private void updateConversationTimestamp(Long conversationId) {
        Conversation update = new Conversation();
        update.setId(conversationId);
        update.setUpdatedAt(LocalDateTime.now());
        conversationMapper.updateById(update);
    }

    private String generateTitle(String message) {
        if (message == null || message.isBlank()) return "New Conversation";
        return message.length() > TITLE_MAX_LENGTH
                ? message.substring(0, TITLE_MAX_LENGTH) + "..."
                : message;
    }

    private ConversationVO toConversationVO(Conversation c) {
        ConversationVO vo = new ConversationVO();
        vo.setId(c.getId());
        vo.setAppId(c.getAppId());
        vo.setUserId(c.getUserId());
        vo.setTitle(c.getTitle());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setUpdatedAt(c.getUpdatedAt());
        return vo;
    }

    private ConversationMessageVO toMessageVO(ConversationMessage m) {
        ConversationMessageVO vo = new ConversationMessageVO();
        vo.setId(m.getId());
        vo.setRole(m.getRole());
        vo.setContent(m.getContent());
        vo.setTokens(m.getTokens());
        vo.setCreatedAt(m.getCreatedAt());
        return vo;
    }
}
