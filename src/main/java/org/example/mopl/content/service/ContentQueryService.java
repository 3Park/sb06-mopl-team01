package org.example.mopl.content.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.request.CursorRequestContentDto;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.dto.response.CursorResponseContentDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.ContentsStat;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.example.mopl.content.repository.ContentTagQueryRepository;
import org.example.mopl.content.repository.ContentsStatQueryRepository;
import org.example.mopl.content.repository.ReviewQueryRepository;
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

  public ContentDto getContentByUuid(UUID uuid) {
    return contentQueryRepository.findByUuidWithContentTag(uuid)
        .orElseThrow(() -> new NoSuchContentException("존재하지 않는 콘텐츠입니다. UUID: " + uuid));
  }

  // Todo : 커서 기반 페이지네이션 (watcherCount로 정렬해야 하므로 실시간 같이보기 모듈 필요)
  // 성능 최적화 필요
  public CursorResponseContentDto getContentsByCursor(CursorRequestContentDto request) {

    Page<Content> contentPage = contentQueryRepository.findAllByCursor(request);

    Map<Long, List<String>> tagListMap = contentTagQueryRepository
        .findTagsByContentIds(
            contentPage.stream()
                .map(Content::getId)
                .toList()
        );

    Map<Long, ContentsStat> contentsStatMap = contentsStatQueryRepository
        .findAllByContentIds(
            contentPage.stream()
                .map(Content::getId)
                .toList()
        );

    Map<Long, Long> contentReviewCountMap = reviewQueryRepository
        .countReviewsByContentIds(
            contentPage.stream()
                .map(Content::getId)
                .toList()
        );

    List<ContentDto> contentDtoList = contentPage.stream()
        .map(content -> {
          ContentsStat contentsStat = contentsStatMap.get(content.getId());
          return ContentDto.of(
              content.getUuid(),
              content.getContentType().getValue(),
              content.getTitle(),
              content.getDescription(),
              content.getThumbnailUrl(),
              tagListMap.getOrDefault(content.getId(), List.of()),
              contentsStat != null ? contentsStat.getRatingAverage() : 0.0,
              contentReviewCountMap.getOrDefault(content.getId(), 0L),
              watchTogetherService.getWatcherCount(String.valueOf(content.getId()))
          );
        })
        .toList();

    return CursorResponseContentDto.builder()
        .data(contentDtoList)
        .nextCursor(contentPage.hasNext() ?
            contentPage.getContent()
                .get(contentPage.getContent().size() - 1).getUuid().toString() : null)
        .nextIdAfter(contentPage.hasNext() ?
            contentPage.getContent()
                .get(contentPage.getContent().size() - 1).getUuid() : null)
        .hasNext(contentPage.hasNext())
        .totalCount(contentPage.getTotalElements())
        .sortBy(request.sortBy())
        .sortDirection(request.sortDirection())
        .build();

  }

}
