package org.example.mopl.content.batch.item;

import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.ContentsWatchingCount;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component("contentWatchingCountItemProcessor")
@RequiredArgsConstructor
public class ContentWatchingCountItemProcessor implements ItemProcessor<ContentsWatchingCount, ContentsWatchingCount> {

  private final WatchTogetherService watchTogetherService;

  @Override
  public @Nullable ContentsWatchingCount process(@NonNull ContentsWatchingCount item) throws Exception {

    item.updateCount(
        watchTogetherService.getWatchingRooms().getOrDefault(item.getContent().getId(), 0L)
    );

    return item;
  }
}
