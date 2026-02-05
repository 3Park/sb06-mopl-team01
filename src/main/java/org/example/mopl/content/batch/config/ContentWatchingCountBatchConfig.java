package org.example.mopl.content.batch.config;

import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.ContentsWatchingCount;
import org.example.mopl.content.repository.ContentsWatchingCountCommandRepository;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Profile("!test")
public class ContentWatchingCountBatchConfig {

  private final ContentsWatchingCountCommandRepository contentsWatchingCountCommandRepository;

  @Bean
  public RepositoryItemReader<ContentsWatchingCount> contentsWatchingCountItemReader() {
    return new RepositoryItemReaderBuilder<ContentsWatchingCount>()
        .name("ContentsWatchingCountItemReader")
        .repository(contentsWatchingCountCommandRepository)
        .methodName("findAll")
        .pageSize(100)
        .build();
  }

  @Bean
  public RepositoryItemWriter<ContentsWatchingCount> contentsWatchingCountItemWriter() {
    return new RepositoryItemWriterBuilder<ContentsWatchingCount>()
        .repository(contentsWatchingCountCommandRepository)
        .build();
  }

}
