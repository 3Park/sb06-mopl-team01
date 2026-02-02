package org.example.mopl.content.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.request.ContentCreateRequest;
import org.example.mopl.content.dto.request.ContentUpdateRequest;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.ContentTag;
import org.example.mopl.content.entity.ContentsStat;
import org.example.mopl.content.entity.Tag;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.exception.NoSuchTagException;
import org.example.mopl.content.mapper.ContentMapper;
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
public class ContentCommandService {

  private final ContentCommandRepository contentCommandRepository;
  private final ContentQueryRepository contentQueryRepository;
  private final TagCommandReposiotry tagCommandReposiotry;
  private final TagQueryRepository tagQueryRepository;
  private final ContentTagCommandRepository contentTagCommandRepository;
  private final ContentsStatCommandRepository contentsStatCommandRepository;
  private final ContentMapper contentMapper;

  @Transactional
  public ContentDto createContent(ContentCreateRequest request) {

    // request에 식별자 없으므로 중복 체크 불가

    // DTO를 엔티티로 변환
    Content content = contentMapper.createRequestToEntity(request);

    //현재 존재하는 태그만 map으로 가져와 비교
    Map<String, Tag> tagMap = tagQueryRepository.findAllByTagNames(request.tags());

    // 없는 태그는 새로 생성
    List<Tag> newTagList = new ArrayList<>();

    request.tags().stream()
        .filter(tagName -> tagMap.get(tagName) == null)
        .forEach(tagName -> newTagList.add(
            Tag.of(tagName)
        ));

    // 없는 태그 일괄 저장
    List<Tag> savedTagList = tagCommandReposiotry.saveAll(newTagList);

    // 콘텐츠 저장
    Content savedContent = contentCommandRepository.save(content);

    // 통계 테이블 저장
    contentsStatCommandRepository.save(
        ContentsStat.of(savedContent)
    );

    //ContentTag 매핑 저장
    List<ContentTag> contentTagList = new ArrayList<>();

    //태그 매핑
    request.tags().forEach(tagName -> {
      Tag tag;
      //기존 태그인 경우
      if(tagMap.get(tagName) != null) {
        tag = tagMap.get(tagName);
      } else { //새로 생성된 태그인 경우
        tag = savedTagList.stream()
            .filter(t -> t.getName().equals(tagName))
            .findFirst()
            .orElseThrow(() ->
                new NoSuchTagException(tagName));
      }
      contentTagList.add(
          ContentTag.of(savedContent, tag)
      );
    });

    // 태그 매핑 저장
    contentTagCommandRepository.saveAll(contentTagList);

    return ContentDto.of(
        savedContent.getUuid(),
        savedContent.getContentType().getValue(),
        savedContent.getTitle(),
        savedContent.getDescription(),
        savedContent.getThumbnailUrl(),
        contentTagList.stream()
            .map(contentTag -> contentTag.getTag().getName())
            .toList(),
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
    contentTagCommandRepository.deleteByContent_Id(content.getId());

    //현재 존재하는 태그만 map으로 가져와 비교
    Map<String, Tag> tagMap = tagQueryRepository.findAllByTagNames(request.tags());

    // 없는 태그는 새로 생성
    List<Tag> newTagList = new ArrayList<>();

    request.tags().stream()
        .filter(tagName -> tagMap.get(tagName) == null)
        .forEach(tagName -> newTagList.add(
            Tag.of(tagName)
        ));

    // 없는 태그 일괄 저장
    List<Tag> savedTagList = tagCommandReposiotry.saveAll(newTagList);

    //태그 매핑
    List<ContentTag> contentTagList = new ArrayList<>();

    request.tags().forEach(tagName -> {
      Tag tag;
      //기존 태그인 경우
      if(tagMap.get(tagName) != null) {
        tag = tagMap.get(tagName);
      } else { //새로 생성된 태그인 경우
        tag = savedTagList.stream()
            .filter(t -> t.getName().equals(tagName))
            .findFirst()
            .orElseThrow(() ->
                new NoSuchTagException(tagName));
      }
      contentTagList.add(
          ContentTag.of(content, tag)
      );
    });

    // 태그 매핑 저장
    contentTagCommandRepository.saveAll(contentTagList);

    // 콘텐츠 저장
    contentCommandRepository.save(content);

    return contentQueryRepository.findByUuidWithContentTag(contentId)
        .orElseThrow(() -> new NoSuchContentException(contentId.toString()));

  }

  @Transactional
  public void deleteContentByUuid(UUID contentUuid) {

    Content content = contentQueryRepository.findByUuid(contentUuid)
        .orElseThrow(() -> new NoSuchContentException(contentUuid.toString()));

    contentTagCommandRepository.deleteByContent_Id(content.getId());
    contentsStatCommandRepository.deleteByContent_id(content.getId());

    contentCommandRepository.delete(content);

  }

}
