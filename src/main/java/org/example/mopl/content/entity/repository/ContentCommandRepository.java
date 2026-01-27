package org.example.mopl.content.entity.repository;

import org.example.mopl.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentCommandRepository extends JpaRepository<Content, Long> {

}
