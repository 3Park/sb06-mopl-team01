package org.example.mopl.content.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentWatchingCountDto {

  public record ContentWatchingCountUpdate(
        Long contentId,
        Long watchingCount
    ) {
      public static ContentWatchingCountUpdate of(Long contentId, Long watchingCount) {
        return new ContentWatchingCountUpdate(contentId, watchingCount);
      }
    }

}
