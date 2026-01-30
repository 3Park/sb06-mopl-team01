package org.example.mopl.content.crawler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.example.mopl.content.dto.ContentFetchResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(
    properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/test",
        "spring.datasource.username=test",
        "spring.datasource.password=test",
        "content.api.tmdb.key=f1c41be844fe207552cf8fbf2849b420",
        "jwt.secret=tem2nx24x340z3sdfd09cc45nc45gnx2n349x4", // 테스트용 시크릿 키
        "spring.data.redis.host=localhost"
    }
)
@DisplayName("TmDbMovieClientTest 통합 테스트")
class TmDbMovieClientTest {

  @Autowired
  private TmDbMovieClient tmDbMovieClient;

  @BeforeEach
  void setUp() {
    // 초기화 작업이 필요한 경우 여기에 작성

    ReflectionTestUtils.setField(tmDbMovieClient, "apiKey", "f1c41be844fe207552cf8fbf2849b420");
    ReflectionTestUtils.setField(tmDbMovieClient, "baseUrl", "https://api.themoviedb.org/3");

  }

  @Test
  @DisplayName("장르 목록 조회 성공")
  void fetchGenres_Success() {

    // given

    // when
    List<String> genres = tmDbMovieClient.fetchGenres();

    // then
    assertNotNull(genres);
    assertFalse(genres.isEmpty());
    assertTrue(genres.contains("Action") || genres.contains("액션"));

  }

  @Test
  @DisplayName("페이지별 콘텐츠 ID 조회 성공")
  void fetchContentIdByPage_Success() {

    // given

    // when
    List<String> contentIds = tmDbMovieClient.fetchContentIdByPage(1);

    // then
    assertNotNull(contentIds);
    assertFalse(contentIds.isEmpty());

  }

  // 구현 필요 없음
  @Test
  @DisplayName("페이지별 콘텐츠 상세 정보 조회 성공")
  void fetchContentsByPageSize_Success() {

    // given

    // when

    // then

  }

  @Test
  @DisplayName("최신 콘텐츠 ID 조회 성공")
  void fetchRecentContentIdByPage_Success() {

    // given

    // when
    List<String> recentContentIds = tmDbMovieClient.fetchRecentContentIdByPage(1);

    // then
    assertNotNull(recentContentIds);

  }

  @Test
  @DisplayName("외부 ID로 콘텐츠 상세 정보 조회 성공")
  void fetchContentDetailsByExternalId_Success() {

    // given
    String externalId = "550"; // Fight Club

    // when
    Optional<ContentFetchResultDto> contentDetails = tmDbMovieClient.fetchContentDetailsByExternalId(externalId);

    // then
    assertTrue(contentDetails.isPresent());
    ContentFetchResultDto result = contentDetails.get();
    assertNotNull(result.externalId());
    assertNotNull(result.title());
    assertNotNull(result.description());

  }
}