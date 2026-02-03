package org.example.mopl.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ContentCreateRequest(
    @NotNull(message = "type은 null일 수 없습니다.")
    String type,
    @NotBlank(message = "title은 blank일 수 없습니다.")
    String title,
    @NotBlank(message = "description은 blank일 수 없습니다.")
    String description,
    @NotNull(message = "tags는 null일 수 없습니다.")
    List<String> tags
) {

}
