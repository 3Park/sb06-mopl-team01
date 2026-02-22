package org.example.mopl.contentevaluation.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.contentevaluation.entity.PlaylistsStat;
import org.example.mopl.contentevaluation.event.SubscribeCountEvent;
import org.example.mopl.contentevaluation.exception.ContentEvaluationErrorCode;
import org.example.mopl.contentevaluation.exception.ContentEvaluationException;
import org.example.mopl.contentevaluation.repository.PlaylistsStatCommandRepository;
import org.example.mopl.contentevaluation.repository.PlaylistsStatQueryRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscribeCountEventListener {

  private final PlaylistsStatCommandRepository playlistsStatCommandRepository;
  private final PlaylistsStatQueryRepository playlistsStatQueryRepository;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void handleIncreaseSubscribeCountEvent(SubscribeCountEvent.IncreaseSubscribeCountEvent event) {

    log.info("구독 수 증가 이벤트 수신 for playlistId: {}", event.playlistId());

    PlaylistsStat playlistsStat = playlistsStatQueryRepository.findByPlaylistId(event.playlistId())
        .orElseThrow(() -> new ContentEvaluationException(ContentEvaluationErrorCode.NO_SUCH_PLAYLIST));

    playlistsStat.incrementSubscribeCount();

    playlistsStatCommandRepository.save(playlistsStat);

    log.info("구독 수 증가 이벤트 처리 완료 for playlistId: {}", event.playlistId());

  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void handleDecreaseSubscribeCountEvent(SubscribeCountEvent.DecreaseSubscribeCountEvent event) {

    log.info("구독 수 감소 이벤트 수신 for playlistId: {}", event.playlistId());

    PlaylistsStat playlistsStat = playlistsStatQueryRepository.findByPlaylistId(event.playlistId())
        .orElseThrow(() -> new ContentEvaluationException(ContentEvaluationErrorCode.NO_SUCH_PLAYLIST));

    playlistsStat.decrementSubscribeCount();

    playlistsStatCommandRepository.save(playlistsStat);

    log.info("구독 수 감소 이벤트 처리 완료 for playlistId: {}", event.playlistId());

  }

}
