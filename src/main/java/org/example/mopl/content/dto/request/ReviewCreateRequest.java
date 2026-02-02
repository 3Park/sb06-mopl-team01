package org.example.mopl.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReviewCreateRequest(
    @NotNull(message = "contentId는 null일 수 없습니다.")
    UUID contentId,
    @NotBlank(message = "text는 blank일 수 없습니다.")
    String text,
    @NotNull(message = "rating은 null일 수 없습니다.")
    Double rating
) {

}
