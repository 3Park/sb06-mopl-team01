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

    return leagues.findValuesAsText("strSport").stream()
        .filter(sport -> sport.equals("Soccer"))
        .toList();

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

    ArrayNode events = (ArrayNode) result.get("events");

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
