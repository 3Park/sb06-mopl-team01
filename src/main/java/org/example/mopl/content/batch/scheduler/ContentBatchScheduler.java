package org.example.mopl.content.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Async("batchTaskExecutor")
  @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시 실행
  public void runContentBatchJob() {
    try {
      log.info("Starting TMDb Content Batch Job");

      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .toJobParameters();

      jobLauncher.run(tmDbBatchConfig.tmDbBatchJob(), jobParameters);

      log.info("Finished TMDb Content Batch Job");
    } catch (Exception e) {
      log.error("Error occurred while running TMDb Content Batch Job", e);
    }
  }

}
