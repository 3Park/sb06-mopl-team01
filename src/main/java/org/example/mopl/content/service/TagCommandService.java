package org.example.mopl.content.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.Tag;
import org.example.mopl.content.repository.TagCommandReposiotry;
import org.example.mopl.content.repository.TagQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagCommandService {

  private final TagCommandReposiotry tagCommandReposiotry;
  private final TagQueryRepository tagQueryRepository;

  @Transactional
  public void createTags(List<String> tagNames) {

    Map<String, Tag> tagMap = tagQueryRepository.findAllMapByNameIn(tagNames);

    // 없는 태그는 새로 생성
    List<Tag> newTagList = new ArrayList<>();

    tagNames.stream()
        .filter(tagName -> tagMap.get(tagName) == null)
        .forEach(tagName -> newTagList.add(
            Tag.of(tagName)
        ));

    // 없는 태그 일괄 저장
    List<Tag> savedTagList = tagCommandReposiotry.saveAll(newTagList);

  }

}
