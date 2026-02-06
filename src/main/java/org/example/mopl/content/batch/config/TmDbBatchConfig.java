package org.example.mopl.content.batch.config;

import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.content.batch.service.TmDbBatchService;
import org.example.mopl.content.dto.ContentFetchResultDto;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
@EnableBatchProcessing
@RequiredArgsConstructor
@Profile("!test")
public class TmDbBatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final TmDbBatchService tmDbBatchService;

  @Value("${content.api.tmdb.all-pages:5}")
  private Integer pageSize;

  @Bean
  public Job tmDbBatchJob() {
    return new JobBuilder("tmDbBatchJob", jobRepository)
        .start(importGenresStep())
        .next(tmDbBatchStep())
        .build();
  }

  @Bean
  public Step importGenresStep() {
    return new StepBuilder("importGenresStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
          tmDbBatchService.importMovieGenres();
          tmDbBatchService.importTvSeriesGenres();
          return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
  }

  @Bean
  public Step tmDbBatchStep() {
    return new StepBuilder("tmDbBatchStep", jobRepository)
        .<Integer, Integer>chunk(1, transactionManager)
        .reader(tmDbPageReader())
        .writer(tmDbPageWriter())
        .build();
  }

  @Bean
  public ItemReader<Integer> tmDbPageReader() {
    // 1부터 pageSize까지의 페이지 번호를 생성하는 Reader
    return new ListItemReader<>(IntStream
        .rangeClosed(1, pageSize)
        .boxed()
        .toList());
  }

  @Bean
  public ItemWriter<Integer> tmDbPageWriter() {
    return pages -> {
      for (Integer page : pages) {

        log.info("Processing TMDb data for page: {}", page);

        // 영화 데이터 가져오기 및 저장
        List<ContentFetchResultDto> movies = tmDbBatchService.importMoviesByPage(page);
        if (!movies.isEmpty()) {
          tmDbBatchService.writeImportedMovies(movies);
          tmDbBatchService.writeImportedContentTags(movies);
        }

        // TV 시리즈 데이터 가져오기 및 저장
        List<ContentFetchResultDto> tvSeries = tmDbBatchService.importTvSeriesByPage(page);
        if (!tvSeries.isEmpty()) {
          tmDbBatchService.writeImportedTvSeries(tvSeries);
          tmDbBatchService.writeImportedContentTags(tvSeries);
        }

      }
    };
  }

}
