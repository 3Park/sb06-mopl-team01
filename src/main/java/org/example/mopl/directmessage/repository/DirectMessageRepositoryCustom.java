package org.example.mopl.directmessage.repository;


import org.example.mopl.directmessage.dto.DirectMessageSearchCondition;
import org.example.mopl.directmessage.entity.DirectMessage;

import java.util.List;
import java.util.Map;

public interface DirectMessageRepositoryCustom {
    Long markAsRead(Long conversationId, Long userId, Long lastMessageId);

    List<DirectMessage> searchByCursor(DirectMessageSearchCondition condition);

    Long countByCursor(DirectMessageSearchCondition condition);

    Map<Long, DirectMessage> findAllLastMessagesByConversationIdIn(List<Long> ids);
}
