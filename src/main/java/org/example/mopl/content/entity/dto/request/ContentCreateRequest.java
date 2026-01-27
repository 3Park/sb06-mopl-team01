package org.example.mopl.content.entity.dto.request;

import java.util.List;

public record ContentCreateRequest(
    String type,
    String title,
    String description,
    List<String> tags
) {

}
