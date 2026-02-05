package org.example.mopl.content.event;

import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchingCountEvent {

  public record updateWatchingCountEvent(
        Map<String, Long> contentWatchingCountMap
    ) {
      public static updateWatchingCountEvent of(Map<String, Long> contentWatchingCountMap) {
        return new updateWatchingCountEvent(contentWatchingCountMap);
      }
    }

}
