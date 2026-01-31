package org.example.mopl.contentevaluation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import org.example.mopl.content.dto.response.ContentDto;

@Builder(access = AccessLevel.PROTECTED)
public record PlaylistDto(
    @JsonProperty(value = "id")
    UUID uuid,
    OwnerDto owner,
    String title,
    String description,
    Instant updatedAt,
    Long subscriberCount,
    Boolean subscribeByMe,
    List<ContentDto> contents
) {

    public static PlaylistDto of(
        UUID uuid,
        OwnerDto owner,
        String title,
        String description,
        Instant updatedAt,
        Long subscriberCount,
        Boolean subscribeByMe,
        List<ContentDto> contents
    ) {
        return PlaylistDto.builder()
            .uuid(uuid)
            .owner(owner)
            .title(title)
            .description(description)
            .updatedAt(updatedAt)
            .subscriberCount(subscriberCount)
            .subscribeByMe(subscribeByMe)
            .contents(contents)
            .build();
    }

}
