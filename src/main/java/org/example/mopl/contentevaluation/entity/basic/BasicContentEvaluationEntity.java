package org.example.mopl.contentevaluation.entity.basic;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BasicContentEvaluationEntity {

  @Id
  @Tsid
  @Column(name = "id", nullable = false)
  protected Long id;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  protected Instant createdAt;

}
