package org.example.mopl.watchtogether.service;

import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.dto.ContentChatSendRequest;
import org.example.mopl.watchtogether.dto.CursorResponseWatchingSessionDto;
import org.example.mopl.watchtogether.dto.WatchingSessionDto;

import java.util.List;
import java.util.UUID;

public interface WatchTogetherService {

    void addUserToRoom(UserDto userDto, String contentId, String sessionId);

    void removeUserFromRoom(String sessionId, UserDto userDto);

    void sendMessageToRoom(String contentId, ContentChatSendRequest message);

    long getWatcherCount(String contentId);

    WatchingSessionDto getWatcher(String watcherId, String sessionId);

    CursorResponseWatchingSessionDto getWatcherList(
            String contentId,
            String watcherNameLike,
            String cursor,
            String idAfter,
            Integer limit,
            String sortDirection,
            String sortBy
    );
}
