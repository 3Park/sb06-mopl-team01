package org.example.mopl.content.repository;

import org.example.mopl.content.entity.ContentsWatchingCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentsWatchingCountCommandRepository extends JpaRepository<ContentsWatchingCount, Long> {

  void deleteByContent_id(Long contentId);

}
