package org.example.mopl.directmessage.repository;


import org.example.mopl.directmessage.dto.condition.DirectMessageSearchCondition;
import org.example.mopl.directmessage.entity.DirectMessage;

import java.util.List;
import java.util.Map;

public interface DirectMessageRepositoryCustom {
    Long readUnreadMessages(Long conversationId, Long userId);

    List<DirectMessage> searchByCursor(DirectMessageSearchCondition condition);

    Long countByConversationId(Long conversationId);

    Map<Long, DirectMessage> findAllLastMessagesByConversationIdIn(List<Long> ids);
}
