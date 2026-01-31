package org.example.mopl.content.event;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RatingEvent {

  public record IncreaseRatingEvent(
      Long contentId,
      UUID contentUuid,
      double rating
  ) {
    public static IncreaseRatingEvent of(Long contentId, UUID contentUuid, double rating) {
      return new IncreaseRatingEvent(contentId, contentUuid, rating);
    }
  }

  public record DecreaseRatingEvent(
      Long contentId,
      UUID contentUuid,
      double rating
  ) {
    public static DecreaseRatingEvent of(Long contentId, UUID contentUuid, double rating) {
      return new DecreaseRatingEvent(contentId, contentUuid, rating);
    }
  }

}
