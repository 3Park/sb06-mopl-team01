package org.example.mopl.content.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.content.dto.request.ContentCreateRequest;
import org.example.mopl.content.dto.request.ContentUpdateRequest;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.ContentTag;
import org.example.mopl.content.entity.Tag;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.exception.NoSuchTagException;
import org.example.mopl.content.mapper.ContentMapper;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.example.mopl.content.repository.ContentTagCommandRepository;
import org.example.mopl.content.repository.TagCommandReposiotry;
import org.example.mopl.content.repository.TagQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentCommandService {

  private final ContentCommandRepository contentCommandRepository;
  private final ContentQueryRepository contentQueryRepository;
  private final TagCommandReposiotry tagCommandReposiotry;
  private final TagQueryRepository tagQueryRepository;
  private final ContentTagCommandRepository contentTagCommandRepository;
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
        .forEach(tagName -> {
          newTagList.add(
              Tag.of(tagName)
          );
        });

    // 없는 태그 일괄 저장
    List<Tag> savedTagList = tagCommandReposiotry.saveAll(newTagList);

    // 콘텐츠 저장
    Content savedContent = contentCommandRepository.save(content);

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
                new NoSuchTagException("태그가 존재하지 않습니다: " + tagName));
      }
      contentTagList.add(
          ContentTag.of(savedContent, tag)
      );
    });

    // 태그 매핑 저장
    contentTagCommandRepository.saveAll(contentTagList);

    return ContentDto.of(
        savedContent.getUuid(),
        savedContent.getType().getValue(),
        savedContent.getTitle(),
        savedContent.getDescription(),
        savedContent.getThumbnailUrl(),
        contentTagList.stream()
            .map(contentTag -> contentTag.getTag().getName())
            .toList(),
        0.0,
        0,
        0L
    );

  }

  @Transactional
  public ContentDto updateContent(UUID contentId, ContentUpdateRequest request) {

    // content 업데이트
    Content content = contentQueryRepository.findByUuid(contentId)
        .orElseThrow(() -> new NoSuchContentException("존재하지 않는 콘텐츠입니다: " + contentId));
    content.update(request.title(), request.description(), request.thumbnailUrl());

    //태그 매핑 일괄 삭제
    contentTagCommandRepository.deleteByContent_Id(content.getId());

    //현재 존재하는 태그만 map으로 가져와 비교
    Map<String, Tag> tagMap = tagQueryRepository.findAllByTagNames(request.tags());

    // 없는 태그는 새로 생성
    List<Tag> newTagList = new ArrayList<>();

    request.tags().stream()
        .filter(tagName -> tagMap.get(tagName) == null)
        .forEach(tagName -> {
          newTagList.add(
              Tag.of(tagName)
          );
        });

    // 없는 태그 일괄 저장
    List<Tag> savedTagList = tagCommandReposiotry.saveAll(newTagList);

    // 콘텐츠 저장
    Content savedContent = contentCommandRepository.save(content);

    return ContentDto.of(
        savedContent.getUuid(),
        savedContent.getType().getValue(),
        savedContent.getTitle(),
        savedContent.getDescription(),
        savedContent.getThumbnailUrl(),
        List.of(),
        0.0,
        0,
        0L
    );

  }

  @Transactional
  public void deleteContentByUuid(UUID contentUuid) {

    Content content = contentQueryRepository.findByUuid(contentUuid)
        .orElseThrow(() -> new NoSuchContentException("존재하지 않는 콘텐츠입니다: " + contentUuid));

    contentTagCommandRepository.deleteByContent_Id(content.getId());

    contentCommandRepository.delete(content);

  }

}
