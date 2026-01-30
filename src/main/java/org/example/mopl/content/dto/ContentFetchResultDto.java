package org.example.mopl.content.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PROTECTED)
public record ContentFetchResultDto(
    String externalId,
    String title,
    String description,
    String thumbnailUrl,
    List<String> tags
) {

  public static ContentFetchResultDto of(
      String externalId,
      String title,
      String description,
      String thumbnailUrl,
      List<String> tags
  ) {
    return ContentFetchResultDto.builder()
        .externalId(externalId)
        .title(title)
        .description(description)
        .thumbnailUrl(thumbnailUrl)
        .tags(tags)
        .build();
  }

}
