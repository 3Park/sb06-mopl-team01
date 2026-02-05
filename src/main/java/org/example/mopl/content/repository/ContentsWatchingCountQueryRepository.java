package org.example.mopl.content.repository;

import java.util.List;
import org.example.mopl.content.entity.ContentsWatchingCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

// Todo : Querydsl 로 변경 고려
@Repository
public interface ContentsWatchingCountQueryRepository extends JpaRepository<ContentsWatchingCount, Long> {

  @Query("SELECT c FROM ContentsWatchingCount c JOIN FETCH c.content")
  List<ContentsWatchingCount> findAll();

}
