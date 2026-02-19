package org.example.mopl.content.batch.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class ContentsJobResultMetrics {

  private final Gauge successGauge;
  private final Counter successCount;
  private final Counter failureCount;
  private final AtomicLong successCounter = new AtomicLong(0);

  public ContentsJobResultMetrics(MeterRegistry meterRegistry) {
    successCount = Counter.builder("contents_job_success_count")
        .description("Contents batch job success count")
        .register(meterRegistry);

    failureCount = Counter.builder("contents_job_failure_count")
        .description("Contents batch job failure count")
        .register(meterRegistry);

    successGauge = Gauge.builder("contents_job_success_processed", successCounter, AtomicLong::get)
        .description("Successfully processed contents count")
        .register(meterRegistry);
  }

  public void jobSuccess() {
    successCount.increment();
  }

  public void jobFailure() {
    failureCount.increment();
  }

  public void updateProcessed(long count) {
    successCounter.set(count);
  }

}
