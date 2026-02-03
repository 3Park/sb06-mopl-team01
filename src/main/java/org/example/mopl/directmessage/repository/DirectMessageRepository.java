package org.example.mopl.directmessage.repository;

import org.example.mopl.directmessage.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {
}
