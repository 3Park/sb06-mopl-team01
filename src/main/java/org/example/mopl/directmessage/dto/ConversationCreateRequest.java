package org.example.mopl.directmessage.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConversationCreateRequest(
        @NotNull UUID withUserId
) {
}
