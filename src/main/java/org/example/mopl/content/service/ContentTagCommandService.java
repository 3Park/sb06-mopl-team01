package org.example.mopl.content.service;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.ContentTag;
import org.example.mopl.content.entity.Tag;
import org.example.mopl.content.repository.ContentTagCommandRepository;
import org.example.mopl.content.repository.ContentTagQueryRepository;
import org.example.mopl.content.repository.TagQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentTagCommandService {

  private final ContentTagCommandRepository contentTagCommandRepository;
  private final ContentTagQueryRepository contentTagQueryRepository;
  private final TagQueryRepository tagQueryRepository;

  @Transactional
  public void createContentTags(Content content, List<String> tagNames) {

    List<ContentTag> contentTagList = new ArrayList<>();

    Map<String, Tag> tagList = tagQueryRepository.findAllByNameIn(tagNames).stream()
        .collect(
            toMap(
                Tag::getName,
                tag -> tag
            )
        );

    Set<String> existingTagNames = contentTagQueryRepository.findTagNamesByContentId(
        content.getId());

    for (String tagName : tagNames) {
      // 기존에 매핑된 태그는 무시
      if (existingTagNames.contains(tagName)) {
        continue;
      }

      Tag tag = tagList.get(tagName);
      contentTagList.add(ContentTag.of(content, tag));

    }

    // 태그 매핑 저장
    contentTagCommandRepository.saveAll(contentTagList);

  }

  @Transactional
  public void deleteByContentId(Long contentId) {
    contentTagCommandRepository.deleteByContent_Id(contentId);
  }

}
