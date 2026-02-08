package org.example.mopl.content.crawler;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.example.mopl.content.dto.ContentFetchResultDto;

public interface MediaCrawlerClient {

  List<String> fetchGenres();

  List<String> fetchContentIdByPage(int pageNumber);

  List<ContentFetchResultDto> fetchContentsByPageSize(int pageNumber, int pageSize);

  List<String> fetchRecentContentIdByPage(int pageNumber);

  CompletableFuture<Optional<ContentFetchResultDto>> fetchContentDetailsByExternalId(String externalId);

}
