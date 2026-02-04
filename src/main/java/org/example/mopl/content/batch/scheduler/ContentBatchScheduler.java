package org.example.mopl.content.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.content.batch.config.TheSportsDbBatchConfig;
import org.example.mopl.content.batch.config.TmDbBatchConfig;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@EnableAsync
@RequiredArgsConstructor
@Profile("!test")
public class ContentBatchScheduler {

  private final JobLauncher jobLauncher;
  private final TmDbBatchConfig tmDbBatchConfig;
  private final TheSportsDbBatchConfig theSportsDbBatchConfig;

  @Async("batchTaskExecutor")
  @Scheduled(initialDelay = 10000, fixedRate = 86400000) // 24 hours
  public void runContentBatchJob() {
    try {
      log.info("Starting TMDb Content Batch Job");

      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis()) // 고유한 JobParameters를 위해 현재 시간을 추가
          .addString("jobName", this.getClass().getSimpleName()) // Job 이름 추가
          .addLong("run.id", System.currentTimeMillis()) // 재실행 가능하도록 유니크 파라미터
          .toJobParameters();

      jobLauncher.run(tmDbBatchConfig.tmDbBatchJob(), jobParameters);

      log.info("Finished TMDb Content Batch Job");

      jobLauncher.run(theSportsDbBatchConfig.theSportsDbBatchJob(), jobParameters);

      log.info("Finished TheSportsDb Content Batch Job");
    } catch (Exception e) {
      log.error("Error occurred while running Content Batch Job", e);
    }
  }

}
