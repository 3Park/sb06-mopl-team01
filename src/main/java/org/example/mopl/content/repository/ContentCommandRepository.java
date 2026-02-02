package org.example.mopl.content.repository;

import org.example.mopl.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentCommandRepository extends JpaRepository<Content, Long> {

    Optional<Content> findByUuid(UUID contentId);
}
