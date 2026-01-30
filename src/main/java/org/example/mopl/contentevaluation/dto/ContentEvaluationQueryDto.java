package org.example.mopl.contentevaluation.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentEvaluationQueryDto {

  @Builder
  public record CursorPlaylistPage(
      Long id,
      UUID uuid,
      UUID userId,
      String userName,
      String userProfileUrl,
      String title,
      String description,
      Instant updatedAt,
      Long subscriberCount,
      Boolean subscribeByMe
  ) {

  }

}
