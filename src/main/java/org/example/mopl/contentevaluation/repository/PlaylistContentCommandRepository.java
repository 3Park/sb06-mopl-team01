package org.example.mopl.contentevaluation.repository;

import org.example.mopl.contentevaluation.entity.PlaylistContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistContentCommandRepository extends JpaRepository<PlaylistContent, Long> {

  void deleteByPlaylist_Id(Long playlistId);

  void deleteByPlaylist_IdAndContent_Id(Long playlistId, Long contentId);

  void deleteByContent_Id(Long contentId);

}
