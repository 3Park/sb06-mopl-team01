package org.example.mopl.contentevaluation.entity.repository;

import org.example.mopl.contentevaluation.entity.PlaylistContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistContentCommandRepository extends JpaRepository<PlaylistContent, Long> {

}
