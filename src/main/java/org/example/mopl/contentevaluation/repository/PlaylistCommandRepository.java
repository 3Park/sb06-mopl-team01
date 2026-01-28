package org.example.mopl.contentevaluation.repository;

import org.example.mopl.contentevaluation.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistCommandRepository extends JpaRepository<Playlist, Long> {

}
