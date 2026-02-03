package org.example.mopl.content.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.content.dto.ContentFetchResultDto;
import org.example.mopl.content.exception.TheSportsDbApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class TheSportsDbSoccerCrawlerClient implements SportCrawlerClient {

  @Value("${content.api.thesportsdb.key}")
  private String apiKey;

  @Value("${content.api.thesportsdb.url}")
  private String baseUrl;

  @Override
  public List<String> fetchLeagues() {

    RestClient restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build();

    JsonNode result = restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/{apiKey}/all_leagues.php")
            .build(apiKey))
        .retrieve()
        .onStatus(status -> !status.is2xxSuccessful(), (request, response) -> {
          log.error("Error while fetching leagues from SportsDb API. Status code: {}", response.getStatusCode().value());
          throw new TheSportsDbApiException(response.getStatusCode().value(), response.getStatusText(), "Failed to fetch leagues from TheSportsDb API");
        })
        .body(JsonNode.class);

    ArrayNode leagues = (ArrayNode) result.get("leagues");

    return leagues.valueStream().filter(
        league -> "Soccer".equals(league.get("strSport").asText())
    )
        .map(league -> league.get("idLeague").asText()).toList();

  }

  @Override
  public List<ContentFetchResultDto> fetchUpcomingEvents(String league) {

    RestClient restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build();

    JsonNode result = restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/{apiKey}/eventsnextleague.php")
            .queryParam("id", league)
            .build(apiKey))
        .retrieve()
        .onStatus(status -> !status.is2xxSuccessful(), (request, response) -> {
          log.error("Error while fetching leagues from SportsDb API. Status code: {}", response.getStatusCode().value());
          throw new TheSportsDbApiException(response.getStatusCode().value(), response.getStatusText(), "Failed to fetch upcoming events from TheSportsDb API");
        })
        .body(JsonNode.class);

    JsonNode eventsNode = result.get("events");

    // null 체크
    if (eventsNode == null || eventsNode.isNull()) {
      log.warn("No events found in API response for league: {}", league);
      return List.of();
    }

    // 타입 체크 추가
    if (!eventsNode.isArray()) {
      log.warn("Events node is not an array, type: {} for league: {}",
          eventsNode.getNodeType(), league);
      return List.of();
    }

    ArrayNode events = (ArrayNode) eventsNode;

    return events.valueStream()
        .map(event -> ContentFetchResultDto.of(
            event.get("idEvent").asText(),
            event.get("strEvent").asText(),
            event.get("dateEvent").asText(),
            event.get("strPoster").asText(),
            List.of(
                event.get("strSport").asText(),
                event.get("strLeague").asText(),
                event.get("strHomeTeam").asText(),
                event.get("strAwayTeam").asText()
            )
        ))
        .toList();

  }
}
