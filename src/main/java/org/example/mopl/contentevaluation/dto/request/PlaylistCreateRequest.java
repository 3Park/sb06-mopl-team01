package org.example.mopl.contentevaluation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PlaylistCreateRequest(
    @NotBlank
    String title,
    @NotBlank
    String description
) {

}
