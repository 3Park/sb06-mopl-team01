package org.example.mopl.content.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RatingEvent {

  public record IncreaseRatingEvent(
      Long contentId,
      double rating
  ) {
    public static IncreaseRatingEvent of(Long contentId, double rating) {
      return new IncreaseRatingEvent(contentId, rating);
    }
  }

  public record DecreaseRatingEvent(
      Long contentId,
      double rating
  ) {
    public static DecreaseRatingEvent of(Long contentId, double rating) {
      return new DecreaseRatingEvent(contentId, rating);
    }
  }

}
