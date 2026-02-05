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
public class ContentsWatchingCountItemProcessor implements ItemProcessor<ContentsWatchingCount, ContentsWatchingCount> {

  private final WatchTogetherService watchTogetherService;

  @Override
  public @Nullable ContentsWatchingCount process(@NonNull ContentsWatchingCount item) throws Exception {

    Long currentCount = watchTogetherService.getWatchingRooms().getOrDefault(item.getContent().getId(), 0L);

    if (item.isSameCount(currentCount)) {
      return null; // 업데이트 필요 없음
    }

    item.updateCount(currentCount);

    return item;
  }
}
