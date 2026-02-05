package org.example.mopl.content.event;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.example.mopl.content.dto.ContentWatchingCountDto.ContentWatchingCountUpdate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchingCountEvent {

  public record UpdateWatchingCountEvent(
        List<ContentWatchingCountUpdate> contentWatchingCountUpdateList
    ) {
      public static UpdateWatchingCountEvent of(List<ContentWatchingCountUpdate> contentWatchingCountUpdateList) {
        return new UpdateWatchingCountEvent(contentWatchingCountUpdateList);
      }
    }

}
