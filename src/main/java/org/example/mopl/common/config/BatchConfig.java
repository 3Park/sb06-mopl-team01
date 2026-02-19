package org.example.mopl.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingAwareMeterObservationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {

  // Batch 작업에서 Micrometer와 Tracing을 통합하여 관측 데이터를 수집하고 추적할 수 있도록 설정
  // Job, Step 등의 Batch 컴포넌트에서 발생하는 이벤트를 관측하여 성능 모니터링과 문제 진단에 활용할 수 있음
  @Bean
  public ObservationRegistry observationRegistry(MeterRegistry meterRegistry, Tracer tracer) {
    DefaultMeterObservationHandler observationHandler = new DefaultMeterObservationHandler(meterRegistry);
    ObservationRegistry observationRegistry = ObservationRegistry.create();
    observationRegistry.observationConfig()
        .observationHandler(new TracingAwareMeterObservationHandler<>(observationHandler, tracer));
    return observationRegistry;
  }

}
