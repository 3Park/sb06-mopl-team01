package org.example.mopl.content.repository;

import org.example.mopl.content.entity.ContentsStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentsStatCommandRepository extends JpaRepository<ContentsStat, Long> {

}
