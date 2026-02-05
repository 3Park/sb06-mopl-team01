package org.example.mopl.content.batch.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentWatchingCountBatchService {

  private final WatchTogetherService watchTogetherService;

  public Map<Long, Long> publishWatchingCountBatchEvent() {

    return watchTogetherService.getWatchingRooms();

  }

}
