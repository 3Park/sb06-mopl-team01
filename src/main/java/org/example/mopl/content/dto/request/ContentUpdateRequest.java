package org.example.mopl.content.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ContentUpdateRequest(
    String title,
    String description,
    List<String> tags,
    @JsonProperty("thumbnail")
    String thumbnailUrl
) {

}
