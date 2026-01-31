package org.example.mopl.watchtogether.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.common.exception.MoplException;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.dto.ContentChatSendRequest;
import org.example.mopl.watchtogether.dto.CursorResponseWatchingSessionDto;
import org.example.mopl.watchtogether.dto.WatchingSessionDto;
import org.example.mopl.watchtogether.model.Watcher;
import org.example.mopl.watchtogether.model.WatchingRoom;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicWatchTogetherService implements WatchTogetherService{

    private final ConcurrentHashMap<String, WatchingRoom> watchingRooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionToRoom = new ConcurrentHashMap<>();

    private final SimpMessageSendingOperations messagingTemplate;
    private final ContentCommandRepository contentCommandRepository;

    @Override
    @Transactional(readOnly = true)
    public void addUserToRoom(UserDto userDto, String contentId, String sessionId) {
        Watcher watcher = new Watcher(userDto);
        WatchingRoom room = watchingRooms.getOrDefault(contentId,null);

        //실시간 같이 시청방 처음 생성시에 컨텐츠 정보 호출
        if(room == null){
            Content content = contentCommandRepository.findByUuid(UUID.fromString(contentId))
                    .orElseThrow(()-> new NoSuchContentException(contentId));
            room = watchingRooms.computeIfAbsent(contentId, w -> new WatchingRoom(content));
            sessionToRoom.put(sessionId,content.getUuid().toString());
        }

        room.addWatcher(watcher);
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
