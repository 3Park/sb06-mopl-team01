package org.example.mopl.content.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record ReviewDto(
    @JsonProperty(value = "id")
    UUID uuid,
    UUID contentId,
    String author,
    String text,
    Double rating
) {

}
