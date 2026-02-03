package org.example.mopl.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.basic.BasicContentUuidEntity;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Entity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tags")
public class Tag extends BasicContentUuidEntity {

  @Column(name = "name", nullable = false)
  private String name;

  @Builder(access = AccessLevel.PRIVATE)
  public Tag(String name) {
    this.name = name;
  }

  public static Tag of(String name) {
    return Tag.builder()
        .name(name)
        .build();
  }

}
