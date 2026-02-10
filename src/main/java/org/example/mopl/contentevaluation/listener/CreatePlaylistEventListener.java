package org.example.mopl.contentevaluation.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.contentevaluation.entity.PlaylistsStat;
import org.example.mopl.contentevaluation.event.CreatePlaylistEvent;
import org.example.mopl.contentevaluation.repository.PlaylistsStatCommandRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreatePlaylistEventListener {

  private final PlaylistsStatCommandRepository playlistsStatCommandRepository;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void handleCreatePlaylistEvent(CreatePlaylistEvent event) {

    log.info("플레이리스트 생성 이벤트 수신 for playlistId: {}", event.playlist().getId());

    // 플레이리스트 통계 초기화
    playlistsStatCommandRepository.save(
        PlaylistsStat.of(event.playlist())
    );

    log.info("플레이리스트 생성 이벤트 처리 완료 for playlistId: {}", event.playlist().getId());

  }

}
