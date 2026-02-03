package org.example.mopl.content.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Profile("!test")
public class TmDbBatchConfig {

  private final JobRepository jobRepository;

  @Bean
  public Job tmDbBatchJob() {
    return new JobBuilder("tmDbBatchJob", jobRepository)
        .start(importGenresStep())
        .next(tmDbBatchStep())
        .build();
  }

  @Bean
  public Step importGenresStep() {
    // Define and return your Step here
    return null; // Placeholder
  }

  @Bean
  public Step tmDbBatchStep() {
    // Define and return your Step here
    return null; // Placeholder
  }

}
