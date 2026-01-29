package org.example.mopl.watchtogether.dto;

import org.example.mopl.watchtogether.model.Watcher;

public record ContentChatDto(
        Watcher sender,
        String content
) {
}
