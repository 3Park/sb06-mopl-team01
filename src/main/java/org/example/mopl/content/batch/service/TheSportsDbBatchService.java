package org.example.mopl.content.batch.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.content.crawler.SportCrawlerClient;
import org.example.mopl.content.dto.ContentFetchResultDto;
import org.example.mopl.content.dto.S3FileDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TheSportsDbBatchService {

  private final SportCrawlerClient theSportsDbSoccerCrawlerClient;
  private final ContentQueryRepository contentQueryRepository;
  private final ContentCommandRepository contentCommandRepository;
  private final ContentTagCommandRepository contentTagCommandRepository;
  private final ContentsStatCommandRepository contentsStatCommandRepository;
  private final ContentsWatchingCountCommandRepository contentsWatchingCountCommandRepository;
  private final TagCommandReposiotry tagCommandReposiotry;
  private final TagQueryRepository tagQueryRepository;
  private final ContentTagQueryRepository contentTagQueryRepository;
  private final ContentS3Client contentS3Client;

  public List<String> importSportLeagues() {
    return theSportsDbSoccerCrawlerClient.fetchLeagues();
  }

  public void writeSportLeagues() {

    List<String> leagueIds = importSportLeagues();

    List<Tag> tagList = new ArrayList<>();

    List<Tag> existingTags = tagQueryRepository.findAllByNameIn(leagueIds);

    leagueIds.forEach(tagName -> {
      boolean exists = existingTags.stream()
          .anyMatch(tag -> tag.getName().equals(tagName));
      if (!exists) {
        tagList.add(Tag.of(tagName));
      }
    });

    tagCommandReposiotry.saveAll(tagList);

  }

  @Transactional
  public void writeSportEvents(String leagueId) {

    List<ContentFetchResultDto> sportEvents = theSportsDbSoccerCrawlerClient.fetchUpcomingEvents(
        leagueId);

    Set<Long> existingContentIds = new HashSet<>(contentQueryRepository.findAllIdsByExternalIds(
        sportEvents.stream()
            .map(ContentFetchResultDto::externalId)
            .toList()
    ));

    List<Content> contentList = new ArrayList<>();
    List<Tag> tagList = new ArrayList<>();
    List<ContentsStat> contentsStatList = new ArrayList<>();
    List<ContentsWatchingCount> contentsWatchingCountList = new ArrayList<>();
    List<ContentTag> contentTagList = new ArrayList<>();

    // 1. 모든 태그 이름 수집
    Set<String> allTagNames = sportEvents.stream()
        .flatMap(sportEvent -> sportEvent.tags().stream())
        .collect(Collectors.toSet());

    // 2. 기존 태그들을 한 번에 조회
    List<Tag> existingTags = tagQueryRepository.findAllByNameIn(allTagNames);
    Set<String> existingTagNames = existingTags.stream()
        .map(Tag::getName)
        .collect(Collectors.toSet());

    // 3. 새로운 태그들만 필터링하여 생성
    List<Tag> newTags = allTagNames.stream()
        .filter(tagName -> !existingTagNames.contains(tagName))
        .map(Tag::of)
        .collect(Collectors.toList());

    if (!newTags.isEmpty()) {
      tagCommandReposiotry.saveAll(newTags);
    }

    tagCommandReposiotry.saveAll(tagList);

    for (ContentFetchResultDto sportEvent : sportEvents) {

      if (contentQueryRepository.existsByExternalId(sportEvent.externalId())) {
        continue;
      }

      String thumbnailUrl;
      try {
        UUID fileUuid = UUID.randomUUID();
        thumbnailUrl = contentS3Client.putObject(
            String.valueOf(fileUuid),
            fetchImageData(sportEvent.thumbnailUrl())
        );
      } catch (Exception e) {
        log.error(e.getMessage());
        throw new S3UploadFailedException(sportEvent.title());
      }

      contentList.add(
          Content.of(
              ContentType.SPORT.getValue(),
              sportEvent.title(),
              sportEvent.description(),
              thumbnailUrl,
              sportEvent.externalId()
          ));

    }

    contentList = contentCommandRepository.saveAll(contentList);

    for (ContentFetchResultDto sportEvent : sportEvents) {

      Content content = contentList.stream()
          .filter(c -> c.getExternalId().equals(sportEvent.externalId()))
          .findFirst()
          .orElseThrow(() -> new NoSuchContentException(sportEvent.externalId()));

      sportEvent.tags().forEach(tagName -> {

        Tag tag = tagQueryRepository.findByName(tagName)
            .orElseThrow(() -> new NoSuchTagException(tagName));

        contentTagList.add(
            ContentTag.of(content, tag)
        );

      });

    }

    contentList.forEach(content -> {

      if (existingContentIds.contains(content.getExternalId())) {
        return;
      }

      contentsStatList.add(
          ContentsStat.of(content)
      );
      contentsWatchingCountList.add(
          ContentsWatchingCount.of(content)
      );
    });

    contentTagCommandRepository.saveAll(contentTagList);
    contentsStatCommandRepository.saveAll(contentsStatList);
    contentsWatchingCountCommandRepository.saveAll(contentsWatchingCountList);

  }

  private S3FileDto fetchImageData(String imageUrl) {

    RestClient restClient = RestClient.builder()
        .baseUrl(imageUrl)
        .build();

    byte[] imageData = restClient.get()
        .retrieve()
        .body(byte[].class);

    // 파일 이름과 콘텐츠 타입 추출
    String fileName = extractFileNameFromUrl(imageUrl);
    String contentType = getContentTypeFromFileName(fileName);

    return S3FileDto.of(
        fileName,
        contentType,
        imageData
    );

  }

  private String extractFileNameFromUrl(String url) {

    if (url == null || url.isEmpty()) {
      return "image.jpg";
    }

    String path = url.substring(url.lastIndexOf('/') + 1);
    if (path.contains("?")) {
      path = path.substring(0, path.indexOf('?'));
    }

    return path.isEmpty() ? "image.jpg" : path;

  }

  private String getContentTypeFromFileName(String fileName) {

    if (fileName == null || fileName.isEmpty()) {
      return "image/jpeg";
    }

    String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

    return switch (extension) {
      case "jpg", "jpeg" -> "image/jpeg";
      case "png" -> "image/png";
      case "gif" -> "image/gif";
      case "webp" -> "image/webp";
      default -> "image/jpeg";
    };

  }

}
