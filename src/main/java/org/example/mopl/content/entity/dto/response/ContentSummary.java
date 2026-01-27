package org.example.mopl.content.entity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ContentSummary(
    @JsonProperty(value = "id")
    UUID uuid,
    String type,
    String title,
    String description,
    String thumbnailUrl,
    List<String> tags,
    Double averageRating,
    Integer reviewCount
) {

}
