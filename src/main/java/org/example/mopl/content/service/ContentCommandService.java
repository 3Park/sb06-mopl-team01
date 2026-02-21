package org.example.mopl.content.service;

import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.ContentQueryDto.ContentWithTagsResult;
import org.example.mopl.content.dto.S3FileDto;
import org.example.mopl.content.dto.request.ContentCreateRequest;
import org.example.mopl.content.dto.request.ContentUpdateRequest;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.event.CreateContentEvent;
import org.example.mopl.content.event.DeleteS3ObjectEvent;
import org.example.mopl.content.exception.ContentErrorCode;
import org.example.mopl.content.exception.ContentException;
import org.example.mopl.content.mapper.ContentMapper;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.example.mopl.content.repository.ContentsStatCommandRepository;
import org.example.mopl.content.repository.ContentsWatchingCountCommandRepository;
import org.example.mopl.content.s3.ContentS3Client;
import org.example.mopl.contentevaluation.repository.PlaylistContentCommandRepository;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ContentCommandService {

  private final ContentCommandRepository contentCommandRepository;
  private final ContentQueryRepository contentQueryRepository;
  private final ContentsStatCommandRepository contentsStatCommandRepository;
  private final ContentsWatchingCountCommandRepository contentsWatchingCountCommandRepository;
  private final PlaylistContentCommandRepository playlistContentCommandRepository;
  private final WatchTogetherService watchTogetherService;
  private final ContentMapper contentMapper;
  public final ApplicationEventPublisher eventPublisher;
  private final ContentS3Client contentS3Client;

  //Content의 자식 엔티티 서비스 클래스
  private final TagCommandService tagCommandService;
  private final ContentTagCommandService contentTagCommandService;

  @Transactional
  public ContentDto createContent(ContentCreateRequest request, MultipartFile thumbnail) {

    // 썸네일 S3 업로드
    String thumbnailUrl;
    try {
      UUID fileUuid = UUID.randomUUID();
      thumbnailUrl = contentS3Client.putObject(
          String.valueOf(fileUuid),
          S3FileDto.of(
              thumbnail.getOriginalFilename(),
              thumbnail.getContentType(),
              thumbnail.getBytes()
          )
      );
    } catch (IOException e) {
      throw new ContentException(ContentErrorCode.S3_UPLOAD_FAILED);
    }

    // DTO를 엔티티로 변환
    Content content = contentMapper.createRequestToEntity(request);
    content.updateThumbnailUrl(thumbnailUrl);

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
        0.0,
        0L,
        0L
    );

  }

  @Transactional
  public ContentDto updateContent(UUID contentId, ContentUpdateRequest request, MultipartFile thumbnail) {

    // content 업데이트
    Content content = contentQueryRepository.findByUuid(contentId)
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_CONTENT));
    content.update(request.title(), request.description());

    // 썸네일 업로드한 경우에만 URL 업데이트
    String currentThumbnailUrl = content.getThumbnailUrl();
    if (thumbnail != null && !thumbnail.isEmpty()) {
      try {
        UUID fileUuid = UUID.randomUUID();
        String thumbnailUrl = contentS3Client.putObject(
            String.valueOf(fileUuid),
            S3FileDto.of(
                thumbnail.getOriginalFilename(),
                thumbnail.getContentType(),
                thumbnail.getBytes()
            )
        );
        content.updateThumbnailUrl(thumbnailUrl);
      } catch (IOException e) {
        throw new ContentException(ContentErrorCode.S3_UPLOAD_FAILED);
      }
    }

    // 기존 태그 매핑 삭제
    contentTagCommandService.deleteByContentId(content.getId());

    // 태그 생성
    tagCommandService.createTags(request.tags());

    // 새로운 ContentTag 매핑 저장
    contentTagCommandService.createContentTags(
        content,
        request.tags()
    );

    // 콘텐츠 저장
    contentCommandRepository.save(content);

    ContentWithTagsResult contentWithTagsResult = contentQueryRepository.findByUuidWithContentTag(contentId)
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_CONTENT));

    // 이전 썸네일 S3에서 삭제하기
    if (thumbnail != null && !thumbnail.isEmpty()) {
      eventPublisher.publishEvent(
          DeleteS3ObjectEvent.of(currentThumbnailUrl)
      );
    }

    return ContentDto.of(
        contentWithTagsResult.uuid(),
        contentWithTagsResult.contentType(),
        contentWithTagsResult.title(),
        contentWithTagsResult.description(),
        contentWithTagsResult.thumbnailUrl(),
        contentWithTagsResult.tags(),
        contentWithTagsResult.averageRating() != null ? contentWithTagsResult.averageRating() : 0.0,
        contentWithTagsResult.reviewCount() != null ? contentWithTagsResult.reviewCount() : 0,
        watchTogetherService.getWatcherCount(String.valueOf(contentWithTagsResult.uuid())
    ));

  }

  @Transactional
  public void deleteContentByUuid(UUID contentUuid) {

    Content content = contentQueryRepository.findByUuid(contentUuid)
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_CONTENT));

    // S3 객체 삭제
    eventPublisher.publishEvent(
        DeleteS3ObjectEvent.of(content.getThumbnailUrl())
    );

    // 연관관계 삭제
    playlistContentCommandRepository.deleteByContent_Id(content.getId());
    contentTagCommandService.deleteByContentId(content.getId());
    contentsStatCommandRepository.deleteByContent_id(content.getId());
    contentsWatchingCountCommandRepository.deleteByContent_id(content.getId());

    // 콘텐츠 삭제
    contentCommandRepository.deleteById(content.getId());

  }

}
