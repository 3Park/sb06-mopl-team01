package org.example.mopl.content.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.content.dto.ContentFetchResultDto;
import org.example.mopl.content.exception.RetryableTmDbApiException;
import org.example.mopl.content.exception.TmDbApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component("tmDbMovieClient")
@Slf4j
@RequiredArgsConstructor
public class TmDbMovieClient implements MediaCrawlerClient {

  @Value("${content.api.tmdb.key}")
  private String apiKey;

  @Value("${content.api.tmdb.url}")
  private String baseUrl;

  @Retryable(
      retryFor = {RetryableTmDbApiException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000, multiplier = 2.0)
  )
  @Override
  public List<String> fetchGenres() {

    try {
      RestClient restClient = RestClient.builder()
          .baseUrl(baseUrl)
          .build();

      JsonNode result = restClient.get()
          .uri(uriBuilder -> uriBuilder.path("/genre/movie/list")
              .queryParam("api_key", apiKey)
              .build())
          .retrieve()
          .onStatus(status -> !status.is2xxSuccessful(), (request, response) -> {
            log.error("Failed to fetch genres from TMDb API. Status code: {}", response.getStatusCode());
            throw new TmDbApiException(response.getStatusCode().value(), response.getStatusText(), "Failed to fetch genres from TMDb API.");
          })
          .body(JsonNode.class);

      return ((ArrayNode) result.get("genres")).findValuesAsText("name");
    } catch (TmDbApiException e) {
      throw RetryableTmDbApiException.createIfRetryable(e);
    }

  }

  @Retryable(
      retryFor = {RetryableTmDbApiException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000, multiplier = 2.0)
  )
  @Override
  public List<String> fetchContentIdByPage(int pageNumber) {

    try {
      RestClient restClient = RestClient.builder()
          .baseUrl(baseUrl)
          .build();

      JsonNode result = restClient.get()
          .uri(uriBuilder -> uriBuilder.path("/discover/movie")
              .queryParam("api_key", apiKey)
              .queryParam("page", pageNumber)
              .build())
          .retrieve()
          .onStatus(status -> !status.is2xxSuccessful(), (request, response) -> {
            log.error("Failed to fetch content id from TMDb API. Status code: {}", response.getStatusCode());
            throw new TmDbApiException(response.getStatusCode().value(), response.getStatusText(), "Failed to fetch content id from TMDb API.");
          })
          .body(JsonNode.class);

      ArrayNode resultArray = (ArrayNode) result.get("results");

      return resultArray.valueStream()
          .map(node -> node.get("id").asText())
          .toList();
    } catch (TmDbApiException e) {
      throw RetryableTmDbApiException.createIfRetryable(e);
    }

  }

  // 구현하지 않음
  @Override
  public List<ContentFetchResultDto> fetchContentsByPageSize(int pageNumber, int pageSize) {
    return List.of();
  }

  @Retryable(
      retryFor = {RetryableTmDbApiException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000, multiplier = 2.0)
  )
  @Override
  public List<String> fetchRecentContentIdByPage(int pageNumber) {

    try {
      RestClient restClient = RestClient.builder()
          .baseUrl(baseUrl)
          .build();

      JsonNode result = restClient.get()
          .uri(uriBuilder -> uriBuilder.path("/movie/changes")
              .queryParam("api_key", apiKey)
              .queryParam("page", pageNumber)
              .build())
          .retrieve()
          .onStatus(status -> !status.is2xxSuccessful(), (request, response) -> {
            log.error("Failed to fetch content id from TMDb API. Status code: {}", response.getStatusCode());
            throw new TmDbApiException(response.getStatusCode().value(), response.getStatusText(), "Failed to fetch content id from TMDb API.");
          })
          .body(JsonNode.class);

      return result.get("results").findValuesAsText("id");
    } catch (TmDbApiException e) {
      throw RetryableTmDbApiException.createIfRetryable(e);
    }

  }

  @Async("tmDbCrawlTaskExecutor")
  @Retryable(
      retryFor = {RetryableTmDbApiException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000, multiplier = 2.0)
  )
  @Override
  public CompletableFuture<Optional<ContentFetchResultDto>> fetchContentDetailsByExternalId(String externalId) {

    try {
      RestClient restClient = RestClient.builder()
          .baseUrl(baseUrl)
          .build();

      JsonNode result = restClient.get()
          .uri(uriBuilder -> uriBuilder.path(String.format("/movie/%s", externalId))
              .queryParam("api_key", apiKey)
              .build())
          .retrieve()
          .onStatus(status -> !status.is2xxSuccessful(), (request, response) -> {
            log.error("Failed to fetch content detail from TMDb API. Status code: {}", response.getStatusCode());
            throw new TmDbApiException(response.getStatusCode().value(), response.getStatusText(), "Failed to fetch content detail from TMDb API.");
          })
          .body(JsonNode.class);

      return CompletableFuture.supplyAsync(() -> Optional.ofNullable(ContentFetchResultDto.of(
          result.get("id").asText(),
          result.get("title").asText(),
          result.get("overview").asText(),
          result.get("poster_path").asText(),
          result.get("genres").findValuesAsText("name")
      )));
    } catch (TmDbApiException e) {
      throw RetryableTmDbApiException.createIfRetryable(e);
    }

  }

}
