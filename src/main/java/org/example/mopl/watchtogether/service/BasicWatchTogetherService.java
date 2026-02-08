package org.example.mopl.watchtogether.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.common.exception.MoplException;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.dto.*;
import org.example.mopl.watchtogether.enumeration.ChangeType;
import org.example.mopl.watchtogether.exception.WatchTogetherErrorCode;
import org.example.mopl.watchtogether.model.Watcher;
import org.example.mopl.watchtogether.model.WatchingRoom;
import org.example.mopl.watchtogether.model.WatchingSession;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicWatchTogetherService implements WatchTogetherService{

    private final ConcurrentHashMap<String, WatchingRoom> watchingRooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionToRoom = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> watcherToSession = new ConcurrentHashMap<>();

    private final SimpMessageSendingOperations messagingTemplate;
    private final ContentCommandRepository contentCommandRepository;

    private final String ASCENDING ="ASCENDING";

    @Override
    @Transactional(readOnly = true)
    public void addUserToRoom(UserDto userDto, String contentId, String sessionId) {
        WatchingRoom room = watchingRooms.get(contentId);

        Watcher watcher = new Watcher(userDto);

        //실시간 같이 시청방 없으면 방생성
        if(room == null){
            Content content = contentCommandRepository.findByUuid(UUID.fromString(contentId))
                    .orElseThrow(()-> new NoSuchContentException(contentId));
            watchingRooms.put(content.getUuid().toString(), new WatchingRoom(content));
            room  = watchingRooms.get(contentId);
        }

        //웹소켓 세션으로 실시간 같이 보기 방 찾을때
        sessionToRoom.put(sessionId,contentId);

        //시청자 아이디로 현재 세션 정보 찾을때
        watcherToSession.put(watcher.getUserId().toString(), sessionId);

        room.addWatcher(new WatchingSession(sessionId,watcher));

        sendWatchingSessionChangeToRoom(
                room.getWatcher(sessionId),
                room.getContent(),
                room.getWatcherCount(),
                ChangeType.JOIN
        );
    }

    @Override
    public void removeUserFromRoom(String sessionId) {
        String roomId = sessionToRoom.get(sessionId);

        if(roomId != null){
            WatchingRoom room = watchingRooms.get(roomId);

            if(room !=null){
                WatchingSession removedWatcher = room.removeWatcher(sessionId);

                if(removedWatcher != null){
                    watcherToSession.remove(removedWatcher.getWatcher().getUserId().toString());
                    sessionToRoom.remove(removedWatcher.getId().toString());

                    sendWatchingSessionChangeToRoom(
                            removedWatcher,
                            room.getContent(),
                            room.getWatcherCount()
                            ,ChangeType.LEAVE
                    );

                    log.info("방 에서 사용자 나감 roomId:{} WatcherId: {}",roomId, removedWatcher.getWatcher().getUserId());

                    if(room.getWatchers().isEmpty()){
                        watchingRooms.remove(roomId);
                        log.info("사용자 없는 방 삭제 roomId: {}", roomId);
                    }
                    return;
                }
            }
            log.info("해당 방이 없습니다 roomId: {}",roomId);
        }
        log.info("해당 세션이 없습니다 sessionId: {}",sessionId);
    }

    @Override
    public void sendMessageToRoom(String contentId, ContentChatSendRequest message, Watcher watcher) {
        if(watchingRooms.containsKey(contentId)){
            String destination = "/sub/contents/"+contentId+"/chat";
            ContentChatDto contentChatDto = new ContentChatDto(watcher,message.content());
            messagingTemplate.convertAndSend(destination,contentChatDto);
        }
    }

    @Override
    public long getWatcherCount(String contentId) {

        if(contentId == null || contentId.isEmpty()) return 0L;

        return Optional.ofNullable(watchingRooms.get(contentId))
                .map(WatchingRoom::getWatcherCount)
                .orElse(0L);
    }

    @Override
    public HashMap<Long,Long> getWatchingRooms() {

        return watchingRooms.values().stream()
                .collect(Collectors.toMap(
                        watchingRoom -> watchingRoom.getContent().getId(),
                        WatchingRoom::getWatcherCount,
                        (oldVal, newVal) -> newVal, // Merge Function (키 중복 시 새 값 사용)
                        HashMap::new
                        )
                );
    }

    @Override
    public WatchingSessionDto getWatcher(String watcherId) {
        String sessionId = watcherToSession.get(watcherId);
        if(sessionId == null){throw new MoplException(WatchTogetherErrorCode.NO_VIEWERS);}

        String roomId = sessionToRoom.get(sessionId);
        WatchingRoom watchingRoom = watchingRooms.get(roomId);

        return WatchingSessionDto.builder()
                .id(watchingRoom.getId())
                .createdAt(watchingRoom.getCreatedAt())
                .watcher(watchingRoom.getWatcher(sessionId).getWatcher())
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
        WatchingRoom room = watchingRooms.get(contentId);
        if(room == null){
            throw new NoSuchContentException(contentId);
        }

        List<WatchingSession> sortedData = sortedData(room.getWatchers(),sortDirection);

        List<WatchingSessionDto> data = filterData(sortedData, cursor, idAfter, limit, room.getContent());

        return CursorResponseWatchingSessionDto.toDto(data,limit,room.getWatcherCount(),sortBy,sortDirection);
    }

    private void sendWatchingSessionChangeToRoom(
            WatchingSession watcher,
            Content content ,
            long WatcherCount,
            ChangeType type){
        WatchingSessionDto watchingSessionDto = new WatchingSessionDto(
                watcher,
                content
        );

        WatchingSessionChange message = WatchingSessionChange.builder()
                .type(type)
                .watchingSession(watchingSessionDto)
                .watcherCount(WatcherCount)
                .build();
        String destination = "/sub/contents/"+content.getUuid()+"/watch";
        messagingTemplate.convertAndSend(destination,message);
    }

    private List<WatchingSession> sortedData (List<WatchingSession> watchingSessions , String sortDirection){

        if(Objects.equals(sortDirection, ASCENDING)){
            return watchingSessions.stream()
                    .sorted(Comparator.comparing(WatchingSession::getCreatedAt))
                    .toList();
        }else {
            return watchingSessions.stream()
                    .sorted(Comparator.comparing(WatchingSession::getCreatedAt).reversed())
                    .toList();
        }
    }

    private List<WatchingSessionDto> filterData(
            List<WatchingSession> sortedData,
            String cursor,
            String idAfter,
            Integer limit,
            Content content
    ){
        if(!cursor.isEmpty() || !idAfter.isEmpty()){

            return sortedData.stream()
                    .dropWhile(watchingSession ->
                            Objects.equals(watchingSession.getWatcher().getName(), cursor) &&
                                    Objects.equals(watchingSession.getWatcher().getUserId().toString(), idAfter))
                    .limit(limit+1)
                    .map(watchingSession -> new WatchingSessionDto(watchingSession,content))
                    .toList();
        }else{
            return sortedData.stream()
                    .limit(limit+1)
                    .map(watchingSession -> new WatchingSessionDto(watchingSession,content))
                    .toList();
        }
    }
}