package org.example.mopl.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingAwareMeterObservationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BatchMetricConfig {

  @Bean
  public ObservationRegistry observationRegistry(MeterRegistry meterRegistry, Tracer tracer) {
    DefaultMeterObservationHandler observationHandler = new DefaultMeterObservationHandler(meterRegistry);
    ObservationRegistry observationRegistry = ObservationRegistry.create();
    observationRegistry.observationConfig()
        .observationHandler(new TracingAwareMeterObservationHandler<>(observationHandler, tracer));
    return observationRegistry;
  }

}
