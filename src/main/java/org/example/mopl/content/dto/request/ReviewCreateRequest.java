package org.example.mopl.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReviewCreateRequest(
    @NotNull
    UUID contentId,
    @NotBlank
    String text,
    @NotNull
    Double rating
) {

}
