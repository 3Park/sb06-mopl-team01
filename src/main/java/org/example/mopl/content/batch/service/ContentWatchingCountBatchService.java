package org.example.mopl.content.batch.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.ContentWatchingCountDto.ContentWatchingCountUpdate;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentWatchingCountBatchService {

  private final WatchTogetherService watchTogetherService;
  private final ApplicationEventPublisher eventPublisher;

  // 실시간 시청자 수 배치 이벤트 발행
  public void publishWatchingCountBatchEvent() {

    List<ContentWatchingCountUpdate> watchingCountUpdateList = watchTogetherService
        .getWatchingRooms().entrySet().stream()
        .map(entry -> {
          return ContentWatchingCountUpdate.of(entry.getKey(), entry.getValue());
        })
        .toList();

  }

}
