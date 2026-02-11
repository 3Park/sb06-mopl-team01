package org.example.mopl.directmessage.dto;

import java.util.List;
import java.util.UUID;

public record CursorResult<T>(
        List<T> items,
        boolean hasNext,
        String nextCursor,
        UUID nextIdAfter
) {
}
