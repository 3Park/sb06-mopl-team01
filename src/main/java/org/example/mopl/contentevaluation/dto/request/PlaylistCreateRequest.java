package org.example.mopl.contentevaluation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PlaylistCreateRequest(
    @NotBlank(message = "title은 blank일 수 없습니다.")
    String title,
    @NotBlank(message = "description은 blank일 수 없습니다.")
    String description
) {

}
