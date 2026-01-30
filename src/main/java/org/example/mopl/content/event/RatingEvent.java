package org.example.mopl.content.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RatingEvent {

  public record IncreaseRatingEvent(
      Long contentId,
      int rating
  ) {
    public static IncreaseRatingEvent of(Long contentId, int rating) {
      return new IncreaseRatingEvent(contentId, rating);
    }
  }

  public record DecreaseRatingEvent(
      Long contentId,
      int rating
  ) {
    public static DecreaseRatingEvent of(Long contentId, int rating) {
      return new DecreaseRatingEvent(contentId, rating);
    }
  }

}
