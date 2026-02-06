package org.example.mopl.content.batch.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.crawler.MediaCrawlerClient;
import org.example.mopl.content.dto.ContentFetchResultDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.ContentTag;
import org.example.mopl.content.entity.ContentType;
import org.example.mopl.content.entity.ContentsStat;
import org.example.mopl.content.entity.ContentsWatchingCount;
import org.example.mopl.content.entity.Tag;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.exception.NoSuchTagException;
import org.example.mopl.content.exception.S3UploadFailedException;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.example.mopl.content.repository.ContentTagCommandRepository;
import org.example.mopl.content.repository.ContentTagQueryRepository;
import org.example.mopl.content.repository.ContentsStatCommandRepository;
import org.example.mopl.content.repository.ContentsWatchingCountCommandRepository;
import org.example.mopl.content.repository.TagCommandReposiotry;
import org.example.mopl.content.repository.TagQueryRepository;
import org.example.mopl.content.s3.ContentS3Client;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class TmDbBatchService {

  private final MediaCrawlerClient tmDbMovieClient;
  private final MediaCrawlerClient tmDbTvSeriesClient;
  private final ContentQueryRepository contentQueryRepository;
  private final ContentCommandRepository contentCommandRepository;
  private final ContentTagCommandRepository contentTagCommandRepository;
  private final ContentsStatCommandRepository contentsStatCommandRepository;
  private final ContentsWatchingCountCommandRepository contentsWatchingCountCommandRepository;
  private final TagCommandReposiotry tagCommandReposiotry;
  private final TagQueryRepository tagQueryRepository;
  private final ContentTagQueryRepository contentTagQueryRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final ContentS3Client contentS3Client;

  public void importMovieGenres() {

    List<String> genres = tmDbMovieClient.fetchGenres();

    List<Tag> tagList = genres.stream()
        .filter(genre -> !tagQueryRepository.existsByName(genre))
        .map(Tag::of)
        .toList();

    tagCommandReposiotry.saveAll(tagList);

  }

  public void importTvSeriesGenres() {

    List<String> genres = tmDbTvSeriesClient.fetchGenres();

    List<Tag> tagList = genres.stream()
        .filter(genre -> !tagQueryRepository.existsByName(genre))
        .map(Tag::of)
        .toList();

    tagCommandReposiotry.saveAll(tagList);

  }

  public List<ContentFetchResultDto> importMoviesByPage(int page) {

    List<String> movieIdList = tmDbMovieClient.fetchContentIdByPage(page);

    // 모든 Future 생성
    List<CompletableFuture<Optional<ContentFetchResultDto>>> futures = movieIdList.stream()
        .map(tmDbMovieClient::fetchContentDetailsByExternalId)
        .toList();

    // 모든 Future가 완료될 때까지 기다림
    CompletableFuture<Void> allFutures = CompletableFuture.allOf(
        futures.toArray(new CompletableFuture[0])
    );

    try {
      // 모든 Future가 완료될 때까지 대기
      allFutures.get();

      // 모든 결과 수집
      return futures.stream()
          .map(future -> {
            try {
              return future.get();
            } catch (Exception e) {
              throw new RuntimeException("Failed to fetch movie details from TMDb", e);
            }
          })
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList());

    } catch (Exception e) {
      throw new RuntimeException("Failed to process movie futures", e);
    }

  }

  public List<ContentFetchResultDto> importTvSeriesByPage(int page) {

    List<String> tvSeriesIdList = tmDbTvSeriesClient.fetchContentIdByPage(page);

    // 모든 Future 생성
    List<CompletableFuture<Optional<ContentFetchResultDto>>> futures = tvSeriesIdList.stream()
        .map(tmDbTvSeriesClient::fetchContentDetailsByExternalId)
        .toList();

    // 모든 Future가 완료될 때까지 기다림
    CompletableFuture<Void> allFutures = CompletableFuture.allOf(
        futures.toArray(new CompletableFuture[0])
    );

    try {
      // 모든 Future가 완료될 때까지 대기
      allFutures.get();

      // 모든 결과 수집
      return futures.stream()
          .map(future -> {
            try {
              return future.get();
            } catch (Exception e) {
              throw new RuntimeException("Failed to fetch TV series details from TMDb", e);
            }
          })
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList());

    } catch (Exception e) {
      throw new RuntimeException("Failed to process TV series futures", e);
    }

  }

  @Transactional
  public void writeImportedGenres(List<String> genres) {

    List<Tag> tagList = new ArrayList<>();

    // 1. 기존 태그들을 한 번에 조회
    List<Tag> existingTags = tagQueryRepository.findAllByNameIn(genres);
    Set<String> existingTagNames = existingTags.stream()
        .map(Tag::getName)
        .collect(Collectors.toSet());

    // 3\2. 새로운 태그들만 필터링하여 생성
    List<Tag> newTags = genres.stream()
        .filter(tagName -> !existingTagNames.contains(tagName))
        .map(Tag::of)
        .collect(Collectors.toList());

    if (!newTags.isEmpty()) {
      tagCommandReposiotry.saveAll(newTags);
    }

  }

  @Transactional
  public void writeImportedMovies(List<ContentFetchResultDto> fetchResultDtoList) {

     List<ContentsStat> contentsStatList = new ArrayList<>();
     List<ContentsWatchingCount> contentsWatchingCountList = new ArrayList<>();

     List<Content> contentList = fetchResultDtoList.stream()
         .map(content -> {

           if (contentQueryRepository.existsByExternalId(content.externalId())) {

             Content existingContent = contentQueryRepository.findByExternalId(content.externalId())
                 .orElseThrow(() -> new NoSuchContentException(content.externalId()));

              existingContent.update(
                  content.title(),
                  content.description()
              );

              return existingContent;

           } else {

             String thumbnailUrl;
             try {
               UUID fileUuid = UUID.randomUUID();
               thumbnailUrl = contentS3Client.putObject(
                   String.valueOf(fileUuid),
                   fetchImageData(content.thumbnailUrl()).getBytes()
               );
             } catch (Exception e) {
               throw new S3UploadFailedException(content.title());
             }

             return Content.of(
                 ContentType.MOVIE.getValue(),
                 content.title(),
                 content.description(),
                 thumbnailUrl,
                 content.externalId()
             );
           }

         })
          .toList();

     contentList = contentCommandRepository.saveAll(contentList);

     contentList.forEach(content -> {
       contentsStatList.add(
           ContentsStat.of(content)
       );
       contentsWatchingCountList.add(
            ContentsWatchingCount.of(content)
       );
     });

     contentsStatCommandRepository.saveAll(contentsStatList);
     contentsWatchingCountCommandRepository.saveAll(contentsWatchingCountList);

  }

  @Transactional
  public void writeImportedTvSeries(List<ContentFetchResultDto> fetchResultDtoList) {

    List<ContentsStat> contentsStatList = new ArrayList<>();

    List<Content> contentList = fetchResultDtoList.stream()
       .map(content -> {

         if (contentQueryRepository.existsByExternalId(content.externalId())) {

           Content existingContent = contentQueryRepository.findByExternalId(content.externalId())
               .orElseThrow(() -> new NoSuchContentException(content.externalId()));

           existingContent.update(
               content.title(),
               content.description()
           );

           return existingContent;

         } else {

           String thumbnailUrl;
           try {
             UUID fileUuid = UUID.randomUUID();
             thumbnailUrl = contentS3Client.putObject(
                 String.valueOf(fileUuid),
                 fetchImageData(content.thumbnailUrl()).getBytes()
             );
           } catch (Exception e) {
             throw new S3UploadFailedException(content.title());
           }

           return Content.of(
               ContentType.TVSERIES.getValue(),
               content.title(),
               content.description(),
               thumbnailUrl,
               content.externalId()
           );
         }

       })
        .toList();

    contentList = contentCommandRepository.saveAll(contentList);

    contentList.forEach(content -> {
      ContentsStat contentsStat = ContentsStat.of(content);
      contentsStatList.add(contentsStat);
    });

    contentsStatCommandRepository.saveAll(contentsStatList);

  }

  @Transactional
  public void writeImportedContentTags(List<ContentFetchResultDto> fetchResultDtoList) {
    List<ContentTag> contentTagList = fetchResultDtoList.stream()
        .flatMap(content -> {
          Content existingContent = contentQueryRepository.findByExternalId(content.externalId())
              .orElseThrow(() -> new NoSuchContentException(content.externalId()));

          return content.tags().stream()
              .map(genre -> {
                Tag tag = tagQueryRepository.findByName(genre)
                    .orElseThrow(() -> new NoSuchTagException(genre));

                return ContentTag.of(existingContent, tag);
              });
        })
        .toList();

    contentTagCommandRepository.saveAll(contentTagList.stream()
        .filter(tag -> !contentTagQueryRepository.existsByContentIdAndTagId(
            tag.getContent().getId(), tag.getTag().getId()))
        .toList());

  }

  private String fetchImageData(String imageUrl) {
    RestClient restClient = RestClient.builder()
        .baseUrl(imageUrl)
        .build();

    byte[] imageData = restClient.get()
        .retrieve()
        .body(byte[].class);

    return Base64.getEncoder().encodeToString(imageData);

  }

}
