package org.example.mopl.watchtogether.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.content.entity.Content;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.dto.ContentChatSendRequest;
import org.example.mopl.watchtogether.dto.CursorResponseWatchingSessionDto;
import org.example.mopl.watchtogether.dto.WatchingSessionDto;
import org.example.mopl.watchtogether.model.Watcher;
import org.example.mopl.watchtogether.model.WatchingRoom;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicWatchTogetherService implements WatchTogetherService{

    private final ConcurrentHashMap<String, WatchingRoom> watchingRooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionToRoom = new ConcurrentHashMap<>();

    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void addUserToRoom(UserDto userDto, String contentId, String sessionId) {
        Watcher watcher = new Watcher(userDto);
        /**
         * 어디서 컨텐츠 호출해야 읜존성 순환 없는지 생각중입니다.
        Content content =

        WatchingRoom room = watchingRooms.computeIfAbsent(contentId, w -> new WatchingRoom(content));
        room.addWatcher(watcher);
        sessionToRoom.put(sessionId,content.getId());
        **/
    }

    @Override
    public void removeUserFromRoom(String sessionId, UserDto userDto) {
        String roomId = sessionToRoom.get(sessionId);
        Watcher watcher = new Watcher(userDto);

        if(roomId != null){
            WatchingRoom room = watchingRooms.get(roomId);
            if(room !=null){
                String watcherId = watcher.getId().toString();
                Watcher removedWatcher = room.removeWatcher(watcherId);
                if(removedWatcher != null){
                    log.info("방 에서 사용자 나감 roomId:{} WatcherId: {}",roomId, watcherId);
                    if(room.isEmpty()){
                        watchingRooms.remove(roomId);
                        log.info("사용자 없는 방 삭제 roomId: {}", roomId);
                    }
                    return;
                }

            }
        }
        log.info("방이 없거나 해당방에 사용자가 없습니다 roomId: {} WatcherId: {}",roomId, watcher.getId());
    }

    @Override
    public void sendMessageToRoom(String contentId, ContentChatSendRequest message) {
        if(watchingRooms.containsKey(contentId)){
            String destination = "/pub/contents/"+contentId+"/chat";
            messagingTemplate.convertAndSend(destination, message);
        }
    }

    @Override
    public long getWatcherCount(String contentId) {
        return watchingRooms.get(contentId).getWatcherCount();
    }

    @Override
    public WatchingSessionDto getWatcher(String watcherId, String sessionId) {
        String roomId = sessionToRoom.get(sessionId);
        WatchingRoom watchingRoom = watchingRooms.get(roomId);

        return WatchingSessionDto.builder()
                .id(watchingRoom.getId())
                .createdAt(watchingRoom.getCreatedAt())
                .watcher(watchingRoom.getWatchers().get(watcherId))
                .content(watchingRoom.getContent())
                .build();
    }

    @Override
    public CursorResponseWatchingSessionDto getWatcherList(
            String contentId,
            String watcherNameLike,
            String cursor,
            String idAfter,
            Integer limit,
            String sortDirection,
            String sortBy
    ) {
        return null;
    }
}
