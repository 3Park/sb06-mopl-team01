package org.example.mopl.content.crawler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.example.mopl.content.dto.ContentFetchResultDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(
    properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/test",
        "spring.datasource.username=test",
        "spring.datasource.password=test",
        "content.api.tmdb.key=",
        "content.api.thesportsdb.key=123", // 테스트용 API 키
        "jwt.secret=tem2nx24x340z3sdfd09cc45nc45gnx2n349x4", // 테스트용 시크릿 키
        "spring.data.redis.host=localhost"
    }
)
class TheSportsDbSoccerCrawlerClientTest {

  @Autowired
  private TheSportsDbSoccerCrawlerClient crawlerClient;

  @Test
  @DisplayName("리그 목록 가져오기 성공")
  void fetchLeagues_Success() {

    // given

    // when
    List<String> leagues = crawlerClient.fetchLeagues();

    // then
    assertNotNull(leagues);
    assertFalse(leagues.isEmpty());

  }

  @Test
  @DisplayName("다가오는 경기 목록 가져오기 성공")
  void fetchUpcomingEvents_Success() {

    // given
    String leagueId = "4328"; // English Premier League

    // when
    List<ContentFetchResultDto> events = crawlerClient.fetchUpcomingEvents(leagueId);

    // then
    assertNotNull(events);
    assertFalse(events.isEmpty());

  }
}