package org.example.mopl.content.crawler;

import java.util.List;
import org.example.mopl.content.dto.response.ContentDto;

public interface MediaCrawlerClient {

  List<String> fetchGenres();

  List<ContentDto> fetchContentsByPage(int pageNumber);

  List<ContentDto> fetchContentsByPageSize(int pageNumber, int pageSize);

  List<ContentDto> fetchRecentContentsByPage(int pageNumber);

  ContentDto fetchContentDetailsByExternalId(String externalId);

}
