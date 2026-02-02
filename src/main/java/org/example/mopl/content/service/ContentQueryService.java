package org.example.mopl.content.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.ContentQueryDto.ContentResult;
import org.example.mopl.content.dto.request.CursorRequestContentDto;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.dto.response.CursorResponseContentDto;
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
        .orElseThrow(() -> new NoSuchContentException(uuid));
  }

  // Todo : 커서 기반 페이지네이션 (watcherCount로 정렬해야 하므로 실시간 같이보기 모듈 필요)
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
            content.thumbnailUrl(),
            tagListMap.getOrDefault(content.id(), List.of()),
            content.averageRating() != null ? content.averageRating() : 0.0,
            content.reviewCount() != null ? content.reviewCount() : 0,
            watchTogetherService.getWatcherCount(String.valueOf(content.id()))
        ))
        .toList();

    return CursorResponseContentDto.builder()
        .data(contentDtoList)
        .nextCursor(contentPage.hasNext() ?
            contentPage.getContent()
                .get(contentPage.getContent().size() - 1).uuid().toString() : null)
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
