package org.example.mopl.content.batch.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.crawler.MediaCrawlerClient;
import org.example.mopl.content.dto.ContentFetchResultDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.ContentTag;
import org.example.mopl.content.entity.ContentType;
import org.example.mopl.content.entity.Tag;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.exception.NoSuchTagException;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.example.mopl.content.repository.ContentTagCommandRepository;
import org.example.mopl.content.repository.ContentsStatCommandRepository;
import org.example.mopl.content.repository.TagCommandReposiotry;
import org.example.mopl.content.repository.TagQueryRepository;
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

  public void importMovieGenres() {

    List<String> genres = tmDbMovieClient.fetchGenres();

    List<Tag> tagList = genres.stream()
        .filter(genre -> !tagQueryRepository.existsByName(genre))
        .map(Tag::of)
        .toList();

    tagCommandReposiotry.saveAll(tagList);

  }

  public void importTvSeriesGenres() {

    List<String> genres = tmDbMovieClient.fetchGenres();

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

    List<Tag> tagList = genres.stream()
        .filter(genre -> !tagQueryRepository.existsByName(genre))
        .map(Tag::of)
        .toList();

    tagCommandReposiotry.saveAll(tagList);

  }

  @Transactional
  public void writeImportedMovies(List<ContentFetchResultDto> fetchResultDtoList) {

   List<Content> contentList = fetchResultDtoList.stream()
       .map(content -> {
         return Content.of(
             ContentType.MOVIE.getValue(),
             content.title(),
             content.description(),
             content.thumbnailUrl(),
             content.externalId()
         );
       })
        .toList();

   contentCommandRepository.saveAll(contentList);

  }

  @Transactional
  public void writeImportedTvSeries(List<ContentFetchResultDto> fetchResultDtoList) {

   List<Content> contentList = fetchResultDtoList.stream()
       .map(content -> {
         return Content.of(
             ContentType.TVSERIES.getValue(),
             content.title(),
             content.description(),
             content.thumbnailUrl(),
             content.externalId()
         );
       })
        .toList();

   contentCommandRepository.saveAll(contentList);

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

    contentTagCommandRepository.saveAll(contentTagList);
  }

}
