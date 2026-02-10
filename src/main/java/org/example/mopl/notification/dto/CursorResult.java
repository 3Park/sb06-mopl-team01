package org.example.mopl.notification.dto;

import java.util.UUID;

public record CursorResult(
        boolean hasNext,
        String nextCursor,
        UUID nextIdAfter
) {
}
