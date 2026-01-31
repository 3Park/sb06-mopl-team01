package org.example.mopl.content.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentQueryDto {

  @Builder
  public record ContentResult(
      Long id,
      UUID uuid,
      String contentType,
      String title,
      String description,
      String thumbnailUrl,
      Instant createdAt,
      Instant updatedAt,
      Double averageRating,
      Long reviewCount
  ) {

  }
  @Builder
  public record ReviewResult(
      UUID uuid,
      UUID contentId,
      UUID userId,
      String userName,
      String userProfileUrl,
      String text,
      Double rating,
      Instant createdAt
  ) {

  }

}
