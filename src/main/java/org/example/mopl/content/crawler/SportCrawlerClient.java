package org.example.mopl.content.crawler;

import java.util.List;
import org.example.mopl.content.dto.ContentFetchResultDto;

public interface SportCrawlerClient {

  List<String> fetchLeagues();

  List<ContentFetchResultDto> fetchUpcomingEvents(String league);

}
