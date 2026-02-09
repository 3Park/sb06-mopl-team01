package org.example.mopl.content.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.content.entity.ContentsStat;
import org.example.mopl.content.entity.ContentsWatchingCount;
import org.example.mopl.content.event.CreateContentEvent;
import org.example.mopl.content.repository.ContentsStatCommandRepository;
import org.example.mopl.content.repository.ContentsWatchingCountCommandRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateContentEventListener {

  private final ContentsStatCommandRepository contentsStatCommandRepository;
  private final ContentsWatchingCountCommandRepository contentsWatchingCountCommandRepository;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void handleCreateContentEvent(CreateContentEvent event) {

    log.info("콘텐츠 생성 이벤트 처리 시작 for contentId: {}", event.content().getId());

    // 콘텐츠 통계 초기화
    contentsStatCommandRepository.save(
        ContentsStat.of(event.content())
    );

    // 콘텐츠 시청 횟수 초기화
    contentsWatchingCountCommandRepository.save(
        ContentsWatchingCount.of(event.content())
    );

    log.info("콘텐츠 생성 이벤트 처리 완료 for contentId: {}", event.content().getId());

  }

}
