package org.example.mopl.content.batch.config;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.batch.item.ContentsWatchingCountItemProcessor;
import org.example.mopl.content.entity.ContentsWatchingCount;
import org.example.mopl.content.repository.ContentsWatchingCountCommandRepository;
import org.example.mopl.content.repository.ContentsWatchingCountQueryRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Profile("!test")
public class ContentsWatchingCountBatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final ContentsWatchingCountItemProcessor contentsWatchingCountItemProcessor;
  private final ContentsWatchingCountQueryRepository contentsWatchingCountQueryRepository;
  private final ContentsWatchingCountCommandRepository contentsWatchingCountCommandRepository;

  @Bean
  public Job contentsWatchingCountJob() {
    return new JobBuilder("contentsWatchingCountJob", jobRepository)
        .start(updateContentsWatchingCountStep())
        .build();
  }

  @Bean
  public Step updateContentsWatchingCountStep() {
    return new StepBuilder("updateContentsWatchingCountStep", jobRepository)
        .<ContentsWatchingCount, ContentsWatchingCount>chunk(1000, transactionManager)
        .reader(contentsWatchingCountItemReader())
        .processor(contentsWatchingCountItemProcessor)
        .writer(contentsWatchingCountItemWriter())
        .build();
  }

  @Bean
  public RepositoryItemReader<ContentsWatchingCount> contentsWatchingCountItemReader() {
    return new RepositoryItemReaderBuilder<ContentsWatchingCount>()
        .name("ContentsWatchingCountItemReader")
        .repository(contentsWatchingCountQueryRepository)
        .methodName("findAll")
        .pageSize(1000)
        .sorts(Map.of("id", Sort.Direction.ASC))
        .build();
  }

  @Bean
  public RepositoryItemWriter<ContentsWatchingCount> contentsWatchingCountItemWriter() {
    return new RepositoryItemWriterBuilder<ContentsWatchingCount>()
        .repository(contentsWatchingCountCommandRepository)
        .build();
  }

}
