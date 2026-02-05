package org.example.mopl.content.batch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.crawler.MediaCrawlerClient;
import org.example.mopl.content.dto.ContentFetchResultDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.ContentTag;
import org.example.mopl.content.entity.ContentType;
import org.example.mopl.content.entity.ContentsStat;
import org.example.mopl.content.entity.Tag;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.exception.NoSuchTagException;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.example.mopl.content.repository.ContentTagCommandRepository;
import org.example.mopl.content.repository.ContentTagQueryRepository;
import org.example.mopl.content.repository.ContentsStatCommandRepository;
import org.example.mopl.content.repository.TagCommandReposiotry;
import org.example.mopl.content.repository.TagQueryRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmDbBatchService {

  private final MediaCrawlerClient tmDbMovieClient;
  private final MediaCrawlerClient tmDbTvSeriesClient;
  private final ContentQueryRepository contentQueryRepository;
  private final ContentCommandRepository contentCommandRepository;
  private final ContentTagCommandRepository contentTagCommandRepository;
  private final ContentsStatCommandRepository contentsStatCommandRepository;
  private final TagCommandReposiotry tagCommandReposiotry;
  private final TagQueryRepository tagQueryRepository;
  private final ContentTagQueryRepository contentTagQueryRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

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

    return movieIdList.stream()
        .map(tmDbMovieClient::fetchContentDetailsByExternalId)
        .map(result -> {
          if (result.isPresent()) {
            return result.get();
          } else {
            throw new RuntimeException("Failed to fetch movie details from TMDb");
          }
        })
        .toList();

  }

  public List<ContentFetchResultDto> importTvSeriesByPage(int page) {

    List<String> tvSeriesIdList = tmDbTvSeriesClient.fetchContentIdByPage(page);

    return tvSeriesIdList.stream()
        .map(tmDbTvSeriesClient::fetchContentDetailsByExternalId)
        .map(result -> {
          if (result.isPresent()) {
            return result.get();
          } else {
            throw new RuntimeException("Failed to fetch TV series details from TMDb");
          }
        })
        .toList();

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
             return Content.of(
                 ContentType.MOVIE.getValue(),
                 content.title(),
                 content.description(),
                 content.thumbnailUrl(),
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

    // Todo : 콘텐츠 시청자 수 테이블 저장

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
           return Content.of(
               ContentType.TVSERIES.getValue(),
               content.title(),
               content.description(),
               content.thumbnailUrl(),
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

}
