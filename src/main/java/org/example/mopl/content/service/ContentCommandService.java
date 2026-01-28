package org.example.mopl.content.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.content.dto.request.ContentCreateRequest;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.content.repository.ContentTagCommandRepository;
import org.example.mopl.content.repository.TagCommandReposiotry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentCommandService {

  private final ContentCommandRepository contentCommandRepository;
  private final TagCommandReposiotry tagCommandReposiotry;
  private final ContentTagCommandRepository contentTagCommandRepository;

  @Transactional
  public ContentDto createContentCommand(
      @Valid @ModelAttribute ContentCreateRequest request
  ) {

    //

  }

}
