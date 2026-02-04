package org.example.mopl.directmessage.repository;


public interface DirectMessageRepositoryCustom {
    Long readUnreadMessages(Long conversationId, Long userId);
}
