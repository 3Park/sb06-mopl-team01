package org.example.mopl.content.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.content.entity.ContentsStat;
import org.example.mopl.content.event.RatingEvent;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.repository.ContentsStatCommandRepository;
import org.example.mopl.content.repository.ContentsStatQueryRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class RatingEventListener {

  private final ContentsStatCommandRepository contentsStatCommandRepository;
  private final ContentsStatQueryRepository contentsStatQueryRepository;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void handleIncreaseRatingEvent(RatingEvent.IncreaseRatingEvent event) {

    log.info("리뷰 수 증가 이벤트 처리 시작 for contentId: {}", event.contentId());

    ContentsStat contentsStat = contentsStatQueryRepository.findByContentId(event.contentId())
        .orElseThrow(() -> new NoSuchContentException(event.contentUuid()));

    contentsStat.addRating((long) event.rating());

    log.info("리뷰 수 증가 이벤트 처리 완료 for contentId: {}", event.contentId());

  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void handleDecreaseRatingEvent(RatingEvent.DecreaseRatingEvent event) {

    log.info("리뷰 수 감소 이벤트 처리 시작 for contentId: {}", event.contentId());

    ContentsStat contentsStat = contentsStatQueryRepository.findByContentId(event.contentId())
        .orElseThrow(() -> new NoSuchContentException(event.contentUuid()));

    contentsStat.removeRating((long) event.rating());

    log.info("리뷰 수 감소 이벤트 처리 완료 for contentId: {}", event.contentId());

  }

}
