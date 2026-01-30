package org.example.mopl.content.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.ContentFetchResultDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TmDbMovieClient implements MediaCrawlerClient {

  @Value("${content.api.tmdb.key}")
  private String apiKey;

  @Value("${content.api.tmdb.url}")
  private String baseUrl;

  @Override
  public List<String> fetchGenres() {

    RestClient restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build();

    JsonNode result = restClient.get()
        .uri(uriBuilder -> uriBuilder.path("/genre/movie/list")
            .queryParam("api_key", apiKey)
            .build())
        .header("Authorization", apiKey)
        .retrieve()
        .body(JsonNode.class);

    if (result == null) {
      return List.of();
    }

    return ((ArrayNode) result.get("genres")).findValuesAsText("name");

  }

  @Override
  public List<String> fetchContentIdByPage(int pageNumber) {

    RestClient restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build();

    JsonNode result = restClient.get()
        .uri(uriBuilder -> uriBuilder.path("/search/movie")
            .queryParam("api_key", apiKey)
            .queryParam("page", pageNumber)
            .build())
        .header("Authorization", apiKey)
        .retrieve()
        .body(JsonNode.class);

    if (result == null) {
      return List.of();
    }

    ArrayNode resultArray = (ArrayNode) result.get("results");

    return resultArray.valueStream()
        .map(node -> node.get("id").asText())
        .toList();

  }

  // 구현하지 않음
  @Override
  public List<ContentFetchResultDto> fetchContentsByPageSize(int pageNumber, int pageSize) {
    return List.of();
  }

  @Override
  public List<String> fetchRecentContentIdByPage(int pageNumber) {

    RestClient restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build();

    JsonNode result = restClient.get()
        .uri(uriBuilder -> uriBuilder.path("/movie/changes")
            .queryParam("api_key", apiKey)
            .queryParam("page", pageNumber)
            .build())
        .header("Authorization", apiKey)
        .retrieve()
        .body(JsonNode.class);

    if (result == null) {
      return List.of();
    }

    return result.get("results").findValuesAsText("id");

  }

  @Override
  public Optional<ContentFetchResultDto> fetchContentDetailsByExternalId(String externalId) {

    RestClient restClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build();

    JsonNode result = restClient.get()
        .uri(uriBuilder -> uriBuilder.path("/movie/" + externalId)
            .queryParam("api_key", apiKey)
            .build())
        .header("Authorization", apiKey)
        .retrieve()
        .body(JsonNode.class);

    if (result != null) {
      return Optional.empty();
    }

    return Optional.ofNullable(ContentFetchResultDto.of(
        result.get("id").asText(),
        result.get("title").asText(),
        result.get("overview").asText(),
        result.get("poster_path").asText(),
        result.get("genres").findValuesAsText("name")
    ));

  }

}
