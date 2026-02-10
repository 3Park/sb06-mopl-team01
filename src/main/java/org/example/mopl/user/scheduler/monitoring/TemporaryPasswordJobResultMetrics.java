package org.example.mopl.user.scheduler.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class TemporaryPasswordJobResultMetrics {
    private final Gauge successGauge;
    private final Counter successCount;
    private final Counter failureCount;
    private final AtomicLong successCounter = new AtomicLong(0);

    public TemporaryPasswordJobResultMetrics(MeterRegistry meterRegistry) {
        successCount = Counter.builder("temporary_password_cleanup_success_count")
                .description("Temporary password batch job success count")
                .register(meterRegistry);

        failureCount = Counter.builder("temporary_password_cleanup_failure_count")
                .description("Temporary password batch job failure count")
                .register(meterRegistry);

        successGauge = Gauge.builder("temporary_password_cleanup_success_processed",successCounter,AtomicLong::get)
                .description("successfully deleted temporary password processed count")
                .register(meterRegistry);
    }

    public void jobSuccess() {
        successCount.increment();
    }

    public void jobFailure() {
        failureCount.increment();
    }

    public void updateProcessed(long count)
    {
        successCounter.set(count);
    }
}
