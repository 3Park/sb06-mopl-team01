package org.example.mopl.content.entity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
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

}
