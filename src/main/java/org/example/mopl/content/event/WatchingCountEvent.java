package org.example.mopl.content.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchingCountEvent {

  public record updateWatchingCountEvent(
        Long contentId,
        long watchingCount
    ) {
      public static updateWatchingCountEvent of(Long contentId, long watchingCount) {
        return new updateWatchingCountEvent(contentId, watchingCount);
      }
    }

}
