package org.example.mopl.content.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/* 무겁고 빈번한 배치 작업은 부하 분산을 위해 다른 인스턴스에서 수행하는 경우도 있음
따라서 추후 Kafka로 전환할 여지를 두기 위해 이벤트 드리븐 방식으로 구현*/
@Slf4j
@RequiredArgsConstructor
@Component
public class WatchingCountEventListener {

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void handleUpdateWatchingCountEvent() {



  }

}
