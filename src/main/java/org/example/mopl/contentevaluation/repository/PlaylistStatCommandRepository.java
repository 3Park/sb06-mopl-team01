package org.example.mopl.contentevaluation.repository;

import org.example.mopl.contentevaluation.entity.PlaylistsStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistStatCommandRepository extends JpaRepository<PlaylistsStat, Long> {

  void deleteByPlaylist_Id(Long playlistId);

}
