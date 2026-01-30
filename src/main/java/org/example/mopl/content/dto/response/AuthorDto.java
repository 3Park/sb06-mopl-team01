package org.example.mopl.content.dto.response;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PROTECTED)
public record AuthorDto(
    UUID userId,
    String name,
    String profileImageUrl
) {

  public static AuthorDto of(UUID userId, String name, String profileImageUrl) {
    return AuthorDto.builder()
        .userId(userId)
        .name(name)
        .profileImageUrl(profileImageUrl)
        .build();
  }

}
