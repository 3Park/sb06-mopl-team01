package org.example.mopl.content.batch.scheduler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.example.mopl.content.batch.config.ContentsWatchingCountBatchConfig;
import org.example.mopl.content.batch.config.TheSportsDbBatchConfig;
import org.example.mopl.content.batch.config.TmDbBatchConfig;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.CompositeTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class ContentBatchSchedulerTest {

  @Autowired
  private JobLauncher jobLauncher;

  @Autowired
  private TmDbBatchConfig tmDbBatchConfig;

  @Autowired
  private TheSportsDbBatchConfig theSportsDbBatchConfig;

  @Autowired
  private ContentsWatchingCountBatchConfig contentsWatchingCountBatchConfig;

  /*@TestConfiguration
  static class TestConfig {
    @Bean(name = "tmDbCrawlTaskExecutor")
    public TaskExecutor tmDbCrawlExecutor() {
      ThreadPoolTaskExecutor executor =new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(6);
      executor.setMaxPoolSize(10);
      executor.setQueueCapacity(100);
      executor.setThreadNamePrefix("tmdb-crawl-task-");
      executor.initialize();

      return executor;
    }
  }*/

  // 성능 테스트
  @Test
  void runTmDbBatchJob_PerformanceTest() throws Exception {
    long startTime = System.currentTimeMillis();

    jobLauncher.run(
        tmDbBatchConfig.tmDbBatchJob(),
        new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters()
    );

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    System.out.println("ContentsWatchingCountJob Duration: " + duration + " ms");

    // 5분 이내에 완료되는지 확인
    //assertTrue(duration < 5 * 60 * 1000, "Job took longer than 5 minutes");
  }

  // 성능 테스트
  @Test
  void runContentsWatchingCountJob_PerformanceTest() throws Exception {
    long startTime = System.currentTimeMillis();

    jobLauncher.run(
        contentsWatchingCountBatchConfig.contentsWatchingCountJob(),
        new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters()
    );

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    System.out.println("ContentsWatchingCountJob Duration: " + duration + " ms");

    // 5분 이내에 완료되는지 확인
    //assertTrue(duration < 5 * 60 * 1000, "Job took longer than 5 minutes");
  }


}