package org.example.mopl.directmessage.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DirectMessageCreateRequest(
        @NotBlank String content
) {
}
