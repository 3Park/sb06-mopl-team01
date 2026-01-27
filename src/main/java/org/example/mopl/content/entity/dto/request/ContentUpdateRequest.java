package org.example.mopl.content.entity.dto.request;

import java.util.List;

public record ContentUpdateRequest(
    String title,
    String description,
    List<String> tags,
    String thumbnail
) {

}
