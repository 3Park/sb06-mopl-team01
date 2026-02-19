package org.example.mopl.contentevaluation.entity.basic;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@MappedSuperclass
public class BasicContentEvaluationUuidEntity extends BasicContentEvaluationEntity{

  @Column(name = "uuid", nullable = false, unique = true)
  protected UUID uuid = UUID.randomUUID();

  @LastModifiedDate
  @Column(name = "updated_at")
  protected Instant updatedAt;

}
