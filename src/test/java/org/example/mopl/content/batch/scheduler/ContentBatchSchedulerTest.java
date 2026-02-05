package org.example.mopl.content.batch.scheduler;

import static org.junit.jupiter.api.Assertions.*;

import org.example.mopl.content.batch.config.ContentsWatchingCountBatchConfig;
import org.example.mopl.content.batch.config.TheSportsDbBatchConfig;
import org.example.mopl.content.batch.config.TmDbBatchConfig;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    assertTrue(duration < 5 * 60 * 1000, "Job took longer than 5 minutes");
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
    assertTrue(duration < 5 * 60 * 1000, "Job took longer than 5 minutes");
  }


}