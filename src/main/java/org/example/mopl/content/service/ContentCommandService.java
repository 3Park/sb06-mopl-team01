package org.example.mopl.content.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.ContentQueryDto.ContentWithTagsResult;
import org.example.mopl.content.dto.request.ContentCreateRequest;
import org.example.mopl.content.dto.request.ContentUpdateRequest;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.ContentsStat;
import org.example.mopl.content.entity.ContentsWatchingCount;
import org.example.mopl.content.event.CreateContentEvent;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.mapper.ContentMapper;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.example.mopl.content.repository.ContentsStatCommandRepository;
import org.example.mopl.content.repository.ContentsWatchingCountCommandRepository;
import org.example.mopl.content.repository.TagQueryRepository;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentCommandService {

  private final ContentCommandRepository contentCommandRepository;
  private final ContentQueryRepository contentQueryRepository;
  private final TagQueryRepository tagQueryRepository;
  private final ContentsStatCommandRepository contentsStatCommandRepository;
  private final ContentsWatchingCountCommandRepository contentsWatchingCountCommandRepository;
  private final WatchTogetherService watchTogetherService;
  private final ContentMapper contentMapper;
  public final ApplicationEventPublisher eventPublisher;

  //Content의 자식 엔티티 서비스 클래스
  private final TagCommandService tagCommandService;
  private final ContentTagCommandService contentTagCommandService;

  @Transactional
  public ContentDto createContent(ContentCreateRequest request) {

    // DTO를 엔티티로 변환
    Content content = contentMapper.createRequestToEntity(request);

    // 태그 생성
    tagCommandService.createTags(request.tags());

    // 콘텐츠 저장
    Content savedContent = contentCommandRepository.save(content);

    //ContentTag 매핑 저장
    contentTagCommandService.createContentTags(
        savedContent,
        request.tags()
    );

    // 통계성 엔티티 생성 이벤트 발행
    eventPublisher.publishEvent(
        CreateContentEvent.of(savedContent)
    );

    return ContentDto.of(
        savedContent.getUuid(),
        savedContent.getContentType().getValue(),
        savedContent.getTitle(),
        savedContent.getDescription(),
        savedContent.getThumbnailUrl(),
        request.tags(),
        0.1,
        0L,
        0L
    );

  }

  @Transactional
  public ContentDto updateContent(UUID contentId, ContentUpdateRequest request) {

    // content 업데이트
    Content content = contentQueryRepository.findByUuid(contentId)
        .orElseThrow(() -> new NoSuchContentException(contentId.toString()));
    content.update(request.title(), request.description());

    //태그 매핑 일괄 삭제
    contentTagCommandService.deleteByContentId(content.getId());

    // 태그 생성
    tagCommandService.createTags(request.tags());

    // 태그 매핑 생성
    contentTagCommandService.createContentTags(content, request.tags());

    // 콘텐츠 저장
    contentCommandRepository.save(content);

    ContentWithTagsResult contentWithTagsResult = contentQueryRepository.findByUuidWithContentTag(contentId)
        .orElseThrow(() -> new NoSuchContentException(contentId.toString()));

    return ContentDto.of(
        contentWithTagsResult.uuid(),
        contentWithTagsResult.contentType(),
        contentWithTagsResult.title(),
        contentWithTagsResult.description(),
        contentWithTagsResult.thumbnailUrl(),
        contentWithTagsResult.tags(),
        contentWithTagsResult.averageRating() != null ? contentWithTagsResult.averageRating() : 0.0,
        contentWithTagsResult.reviewCount() != null ? contentWithTagsResult.reviewCount() : 0,
        watchTogetherService.getWatcherCount(String.valueOf(contentWithTagsResult.id())
    ));

  }

  @Transactional
  public void deleteContentByUuid(UUID contentUuid) {

    Content content = contentQueryRepository.findByUuid(contentUuid)
        .orElseThrow(() -> new NoSuchContentException(contentUuid.toString()));

    // 연관관계 삭제
    contentTagCommandService.deleteByContentId(content.getId());
    contentsStatCommandRepository.deleteByContent_id(content.getId());
    contentsWatchingCountCommandRepository.deleteByContent_id(content.getId());

    // 콘텐츠 삭제
    contentCommandRepository.deleteById(content.getId());

  }

}
