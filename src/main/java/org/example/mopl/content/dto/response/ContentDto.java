package org.example.mopl.content.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder(access = AccessLevel.PROTECTED)
public record ContentDto(
    @JsonProperty(value = "id")
    UUID uuid,
    String type,
    String title,
    String description,
    @Nullable
    String thumbnailUrl,
    List<String> tags,
    Double averageRating,
    Integer reviewCount,
    Long watcherCount
) {

    public static ContentDto of (
        UUID uuid,
        String type,
        String title,
        String description,
        @Nullable
        String thumbnailUrl,
        List<String> tags,
        Double averageRating,
        Integer reviewCount,
        Long watcherCount
    ) {
        return ContentDto.builder()
            .uuid(uuid)
            .type(type)
            .title(title)
            .description(description)
            .thumbnailUrl(thumbnailUrl)
            .tags(tags)
            .averageRating(averageRating)
            .reviewCount(reviewCount)
            .watcherCount(watcherCount)
            .build();
    }

}
