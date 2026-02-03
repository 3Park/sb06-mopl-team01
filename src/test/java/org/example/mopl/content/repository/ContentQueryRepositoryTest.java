package org.example.mopl.content.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.example.mopl.MoplApplication;
import org.example.mopl.common.config.QueryDslConfig;
import org.example.mopl.content.dto.ContentQueryDto.ContentResult;
import org.example.mopl.content.dto.request.CursorRequestContentDto;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.entity.ContentsStat;
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, MoplApplication.class, ContentQueryRepository.class})
@DisplayName("ContentQueryRepository 테스트")
class ContentQueryRepositoryTest {

  @Autowired
  private ContentQueryRepository contentQueryRepository;

  @Autowired
  private ContentCommandRepository contentCommandRepository;

  @Autowired
  private ContentsStatCommandRepository contentsStatCommandRepository;

  @MockitoBean
  private WatchTogetherService watchTogetherService;

  private List<Content> testContents = new ArrayList<>();

  @BeforeEach
  void setUp() {

    for (int i = 1; i <= 25; i++) {
      Content content = Content.of(
          "MOVIE",
          "Test Movie " + i,
          "A description for Test Movie " + i,
          null
      );
      testContents.add(content);
    }

    testContents = contentCommandRepository.saveAll(testContents);

    List<ContentsStat> contentsStats = testContents.stream()
        .map(ContentsStat::of)
        .toList();
    contentsStatCommandRepository.saveAll(contentsStats);

  }

  @AfterEach
  void tearDown() {
    contentsStatCommandRepository.deleteAll();
    contentCommandRepository.deleteAll();
  }

  @Test
  void findAllByCursor() {

    // given
    CursorRequestContentDto request = new CursorRequestContentDto(
        "MOVIE",
        "Test",
        new ArrayList<>(),
        null,
        null,
        10,
        "DESC",
        "createdAt"
    );

    when(watchTogetherService.getWatcherCount(any())).thenReturn(1L);

    // when
    Page<ContentResult> result = contentQueryRepository.findAllByCursor(request);

    // then
    assertEquals(10, result.getContent().size());

  }
}