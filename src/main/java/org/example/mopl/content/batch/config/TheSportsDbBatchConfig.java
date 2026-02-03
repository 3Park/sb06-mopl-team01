package org.example.mopl.content.batch.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.batch.service.TheSportsDbBatchService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Profile("!test")
public class TheSportsDbBatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final TheSportsDbBatchService theSportsDbBatchService;

  @Bean
  public Job theSportsDbBatchJob() {
    return new JobBuilder("theSportsDbBatchJob", jobRepository)
        .start(importSportsStep())
        .build();
  }

  @Bean
  public Step importSportsStep() {
    return new StepBuilder("importSportsStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {

          List<String> leagues = theSportsDbBatchService.importSportLeagues();

          for (String league : leagues) {
            theSportsDbBatchService.writeSportEvents(league);
          }
          return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
  }

}
