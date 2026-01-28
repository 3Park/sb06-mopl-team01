package org.example.mopl.content.repository;

import org.example.mopl.content.entity.ContentTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentTagCommandRepository extends JpaRepository<ContentTag, Long> {

}
