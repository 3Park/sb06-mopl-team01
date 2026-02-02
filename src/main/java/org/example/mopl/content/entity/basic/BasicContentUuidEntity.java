package org.example.mopl.content.entity.basic;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@MappedSuperclass
public class BasicContentUuidEntity extends BasicContentEntity {

  @UuidGenerator
  @Column(name = "uuid", nullable = false, unique = true)
  protected UUID uuid;

  @LastModifiedDate
  @Column(name = "updated_at")
  protected Instant updatedAt;

}
