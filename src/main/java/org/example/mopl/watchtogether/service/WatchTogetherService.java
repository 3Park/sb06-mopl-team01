package org.example.mopl.watchtogether.service;

import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.dto.ContentChatSendRequest;
import org.example.mopl.watchtogether.dto.CursorResponseWatchingSessionDto;
import org.example.mopl.watchtogether.dto.WatchingSessionDto;
import org.example.mopl.watchtogether.model.Watcher;

import java.util.HashMap;

public interface WatchTogetherService {

    void addUserToRoom(UserDto userDto, String contentId, String sessionId);

    void removeUserFromRoom(String sessionId);

    void sendMessageToRoom(String contentId, ContentChatSendRequest message, Watcher watcher);

    long getWatcherCount(String contentId);

    HashMap<Long, Long> getWatchingRooms();

    WatchingSessionDto getWatcher(String watcherId);

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
