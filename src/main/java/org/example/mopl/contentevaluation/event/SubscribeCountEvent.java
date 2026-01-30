package org.example.mopl.contentevaluation.event;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscribeCountEvent {

  public record IncreaseSubscribeCountEvent(
      Long playlistId,
      UUID playlistUuid
  ) {
    public static IncreaseSubscribeCountEvent of(Long playlistId, UUID playlistUuid) {
      return new IncreaseSubscribeCountEvent(playlistId, playlistUuid);
    }
  }

  public record DecreaseSubscribeCountEvent(
      long playlistId,
      UUID playlistUuid
  ) {
    public static DecreaseSubscribeCountEvent of(Long playlistId, UUID playlistUuid) {
      return new DecreaseSubscribeCountEvent(playlistId, playlistUuid);
    }
  }

}
