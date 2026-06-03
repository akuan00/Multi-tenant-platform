package com.company.ai.platform.conversation;

import com.company.ai.platform.conversation.entity.ConversationMessage;
import com.company.ai.platform.conversation.model.ConversationMessageVO;
import com.company.ai.platform.conversation.model.ConversationVO;
import com.company.ai.platform.conversation.model.CreateConversationRequest;

import java.util.List;

public interface ConversationService {

    ConversationVO createConversation(String appId, CreateConversationRequest request);

    List<ConversationVO> listConversations(String appId, String userId);

    ConversationVO getConversation(String appId, Long conversationId);

    void deleteConversation(String appId, Long conversationId);

    List<ConversationMessageVO> getMessages(String appId, Long conversationId);

    ConversationMessage saveUserMessage(Long conversationId, String content);

    ConversationMessage saveAssistantMessage(Long conversationId, String content, Integer tokens);

    Long ensureConversation(String appId, Long conversationId, String userId, String firstMessage);
}
