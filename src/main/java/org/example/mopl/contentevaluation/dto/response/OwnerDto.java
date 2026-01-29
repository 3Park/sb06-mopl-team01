package org.example.mopl.contentevaluation.dto.response;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PROTECTED)
public record OwnerDto(
    UUID userId,
    String name,
    String profileImageUrl
) {

  public static OwnerDto of(UUID userId, String name, String profileImageUrl) {
    return OwnerDto.builder()
        .userId(userId)
        .name(name)
        .profileImageUrl(profileImageUrl)
        .build();
  }

}
