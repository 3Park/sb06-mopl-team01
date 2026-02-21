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
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.support.RetryTemplate;
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

  // 리그가 9개 정도 밖에 안되서 chunk 단위 처리 의미 없음
  @Bean
  public Step importSportsStep() {
    return new StepBuilder("importSportsStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
          RetryTemplate retryTemplate = RetryTemplate.builder()
              .maxAttempts(5)
              .retryOn(PessimisticLockingFailureException.class)
              .exponentialBackoff(1000, 2, 10000)
              .build();

          return retryTemplate.execute(context -> {
            List<String> leagues = theSportsDbBatchService.importSportLeagues();

            for (String league : leagues) {
              System.out.println("Importing league " + league);
              theSportsDbBatchService.writeSportEvents(league);
            }
            return RepeatStatus.FINISHED;
          });
        }, transactionManager)
        .build();
  }

}
