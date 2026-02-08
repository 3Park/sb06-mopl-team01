package org.example.mopl.content.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.ContentQueryDto.ContentResult;
import org.example.mopl.content.dto.ContentQueryDto.ContentWithTagsResult;
import org.example.mopl.content.dto.request.CursorRequestContentDto;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.dto.response.CursorResponseContentDto;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.example.mopl.content.repository.ContentTagQueryRepository;
import org.example.mopl.content.repository.ContentsStatQueryRepository;
import org.example.mopl.content.repository.ReviewQueryRepository;
import org.example.mopl.content.s3.ContentS3Client;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentQueryService {

  private final ContentQueryRepository contentQueryRepository;
  private final ContentTagQueryRepository contentTagQueryRepository;
  private final ContentsStatQueryRepository contentsStatQueryRepository;
  private final WatchTogetherService watchTogetherService;
  private final ReviewQueryRepository reviewQueryRepository;
  private final ContentS3Client contentS3Client;

  public ContentDto getContentByUuid(UUID uuid) {

    ContentWithTagsResult content = contentQueryRepository.findByUuidWithContentTag(uuid)
        .orElseThrow(() -> new NoSuchContentException(uuid));

    return ContentDto.of(
        content.uuid(),
        content.contentType(),
        content.title(),
        content.description(),
        contentS3Client.getPresignedUrl(content.thumbnailUrl()),
        content.tags(),
        content.averageRating(),
        content.reviewCount(),
        watchTogetherService.getWatcherCount(String.valueOf(content.id()))
    );

  }

  public CursorResponseContentDto getContentsByCursor(CursorRequestContentDto request) {

    Page<ContentResult> contentPage = contentQueryRepository
        .findAllByCursor(request);

    Map<Long, List<String>> tagListMap = contentTagQueryRepository
        .findTagsByContentIds(
            contentPage.getContent().stream()
                .map(ContentResult::id)
                .toList()
        );

    List<ContentDto> contentDtoList = contentPage.getContent().stream()
        .map(content -> ContentDto.of(
            content.uuid(),
            content.contentType(),
            content.title(),
            content.description(),
            contentS3Client.getPresignedUrl(content.thumbnailUrl()),
            tagListMap.getOrDefault(content.id(), List.of()),
            content.averageRating() != null ? content.averageRating() : 0.0,
            content.reviewCount() != null ? content.reviewCount() : 0,
            content.watcherCount()
        ))
        .toList();

    return CursorResponseContentDto.builder()
        .data(contentDtoList)
        .nextCursor(contentPage.hasNext() ?
            switch (request.sortBy()) {
              case "watcherCount" -> contentPage.getContent()
                  .get(contentPage.getContent().size() - 1).watcherCount().toString();
              case "rate" -> contentPage.getContent()
                  .get(contentPage.getContent().size() - 1).averageRating().toString();
              default -> contentPage.getContent()
                  .get(contentPage.getContent().size() - 1).createdAt().toString();
            } : null)
        .nextIdAfter(contentPage.hasNext() ?
            contentPage.getContent()
                .get(contentPage.getContent().size() - 1).uuid() : null)
        .hasNext(contentPage.hasNext())
        .totalCount(contentPage.getTotalElements())
        .sortBy(request.sortBy())
        .sortDirection(request.sortDirection())
        .build();

  }

}
