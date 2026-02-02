package org.example.mopl.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Entity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contents")
public class Content {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @UuidGenerator
  @Column(name = "uuid", nullable = false, unique = true)
  private UUID uuid;

  @Column(name = "external_id", nullable = true, unique = true)
  private String externalId;

  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private ContentType contentType;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "thumbnail_url", nullable = true)
  private String thumbnailUrl;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Builder(access = AccessLevel.PROTECTED)
  public Content(String type, String title, String description, String thumbnailUrl) {
    this.contentType = ContentType.fromValue(type);
    this.title = title;
    this.description = description;
    this.thumbnailUrl = thumbnailUrl;
  }

  public static Content of(String type, String title, String description, String thumbnailUrl) {
    return Content.builder()
        .type(type)
        .title(title)
        .description(description)
        .thumbnailUrl(thumbnailUrl)
        .build();
  }

  public void update(
      String title,
      String description
  ) {
    this.title = title;
    this.description = description;
  }

}
