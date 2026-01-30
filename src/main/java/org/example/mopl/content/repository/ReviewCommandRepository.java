package org.example.mopl.content.repository;

import java.util.UUID;
import org.example.mopl.content.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewCommandRepository extends JpaRepository<Review, Long> {

  void deleteByUuid(UUID reviewId);

}
