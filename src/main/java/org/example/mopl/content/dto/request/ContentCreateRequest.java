package org.example.mopl.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ContentCreateRequest(
    @NotNull
    String type,
    @NotBlank
    String title,
    @NotBlank
    String description,
    @NotNull
    List<String> tags
) {

}
