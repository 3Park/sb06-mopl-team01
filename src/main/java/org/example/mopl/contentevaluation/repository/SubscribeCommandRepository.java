package org.example.mopl.contentevaluation.repository;

import org.example.mopl.contentevaluation.entity.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscribeCommandRepository extends JpaRepository<Subscribe, Long> {

  void deleteByUser_IdAndPlaylist_Id(Long userId, Long playlistId);

}
