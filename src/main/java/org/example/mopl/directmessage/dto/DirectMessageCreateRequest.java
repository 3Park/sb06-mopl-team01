package org.example.mopl.directmessage.dto;

import jakarta.validation.constraints.NotBlank;

public record DirectMessageCreateRequest(
        @NotBlank String content
) {
}
