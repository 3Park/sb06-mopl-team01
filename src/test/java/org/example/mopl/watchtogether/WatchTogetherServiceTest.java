package org.example.mopl.watchtogether;

import org.example.mopl.content.entity.Content;
import org.example.mopl.content.exception.ContentException;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.profile.repository.FollowRepository;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.dto.CursorResponseWatchingSessionDto;
import org.example.mopl.watchtogether.model.Watcher;
import org.example.mopl.watchtogether.model.WatchingSession;
import org.example.mopl.watchtogether.service.BasicWatchTogetherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.*;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WatchTogetherServiceTest {

    private BasicWatchTogetherService watchTogetherService;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private ContentCommandRepository contentCommandRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private RedisTemplate<String, Object> watchTogetherRedisTemplate;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @Mock
    private ValueOperations<String, String> stringValueOps;

    @Mock
    private SetOperations<String, String> setOps;

    @Mock
    private ZSetOperations<String, String> zSetOps;

    @BeforeEach
    void setUp() {
        lenient().when(watchTogetherRedisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(stringValueOps);
        lenient().when(stringRedisTemplate.opsForSet()).thenReturn(setOps);
        lenient().when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOps);

        watchTogetherService = new BasicWatchTogetherService(
                watchTogetherRedisTemplate,
                stringRedisTemplate,
                messagingTemplate,
                contentCommandRepository,
                followRepository,
                applicationEventPublisher
        );

    }


    private Content createMockContent(Long id, UUID uuid) {
        Content content = Content.of("MOVIE", "Test Title", "Test Description", "http://test.url");
        ReflectionTestUtils.setField(content, "id", id);
        ReflectionTestUtils.setField(content, "uuid", uuid);
        return content;
    }

    private UserDto createMockUserDto(UUID userId, String name) {
        UserDto userDto = mock(UserDto.class);
        given(userDto.getId()).willReturn(userId);
        given(userDto.getName()).willReturn(name);
        return userDto;
    }

    @Test
    @DisplayName("방 입장: 방이 없을 경우 활성 목록에 추가하고 Redis에 세션 정보를 저장한다.")
    void addUserToRoom_createRoom() {
        // given
        String contentUuidStr = UUID.randomUUID().toString();
        String sessionId = "session-123";
        UUID userId = UUID.randomUUID();

        UserDto userDto = createMockUserDto(userId, "Tester");
        Content content = createMockContent(1L, UUID.fromString(contentUuidStr));

        // 기존 세션 없음
        given(stringValueOps.get(anyString())).willReturn(null);
        // 방 없음
        given(stringRedisTemplate.hasKey(anyString())).willReturn(false);
        given(contentCommandRepository.findByUuid(any())).willReturn(Optional.of(content));
        // 인원수 리턴
        given(zSetOps.zCard(anyString())).willReturn(1L);

        // when
        watchTogetherService.addUserToRoom(userDto, contentUuidStr, sessionId);

        // then
        verify(setOps).add(eq("active_rooms"), eq(contentUuidStr));
        verify(valueOps).set(eq("session:" + sessionId), any(WatchingSession.class), any());
        verify(stringValueOps).set(eq("session:" + sessionId + ":room"), eq(contentUuidStr), any());
        verify(stringValueOps).set(eq("user:" + userId), eq(sessionId), any());
        verify(zSetOps).add(eq("room:" + contentUuidStr + ":watchers"), eq(sessionId), anyDouble());
        verify(messagingTemplate).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("시청자 수 조회: Redis ZSet의 zCard를 호출한다.")
    void getWatcherCount_NullSafe() {
        // given
        String contentId = "test-content";
        given(zSetOps.zCard(anyString())).willReturn(5L);

        // when
        long count = watchTogetherService.getWatcherCount(contentId);

        // then
        assertThat(count).isEqualTo(5L);
        assertThat(watchTogetherService.getWatcherCount(null)).isZero();
        assertThat(watchTogetherService.getWatcherCount("")).isZero();
    }

    @Test
    @DisplayName("방 퇴장: 유저가 나가고 방에 사람이 없으면 활성 목록에서 제거된다.")
    void removeUserFromRoom_deleteRoom() {
        // given
        String sessionId = "session-123";
        String contentId = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();

        Watcher watcher = new Watcher(createMockUserDto(userId, "Tester"));
        WatchingSession session = new WatchingSession(sessionId, watcher);
        Content content = createMockContent(1L, UUID.fromString(contentId));

        // 세션 객체 조회 (redisTemplate -> valueOps)
        // 키: "session:session-123"
        given(valueOps.get("session:" + sessionId)).willReturn(session);

        // 방 ID 조회 (stringRedisTemplate -> stringValueOps)
        // 키: "session:session-123:room"
        given(stringValueOps.get("session:" + sessionId + ":room")).willReturn(contentId);

        // 방 인원 0명 (삭제 시나리오)
        given(zSetOps.zCard("room:" + contentId + ":watchers")).willReturn(0L);
        given(contentCommandRepository.findByUuid(UUID.fromString(contentId))).willReturn(Optional.of(content));

        // when
        watchTogetherService.removeUserFromRoom(sessionId);

        // then
        verify(zSetOps).remove(anyString(), eq(sessionId));
        verify(watchTogetherRedisTemplate).delete("session:" + sessionId);
        verify(setOps).remove("active_rooms", contentId);
        verify(messagingTemplate).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("시청자 목록 조회: Redis에서 정렬된 ID를 가져와 상세 정보를 조회한다.")
    void getWatcherList_SortedAndFiltered() {
        // given
        String contentUuidStr = UUID.randomUUID().toString();
        String idAfter = UUID.randomUUID().toString();
        Content content = createMockContent(1L, UUID.fromString(contentUuidStr));

        given(contentCommandRepository.findByUuid(any())).willReturn(Optional.of(content));
        given(zSetOps.zCard(anyString())).willReturn(10L);

        // idAfter에 해당하는 세션 ID 반환
        given(stringValueOps.get("user:" + idAfter)).willReturn("s1");

        // 전체 명단 (ZSet range)
        Set<String> allSessionIds = new LinkedHashSet<>(Arrays.asList("s1", "s2", "s3", "s4"));
        given(zSetOps.reverseRange(anyString(), anyLong(), anyLong())).willReturn(allSessionIds);

        // MultiGet 결과
        WatchingSession s2 = new WatchingSession("s2", new Watcher(createMockUserDto(UUID.randomUUID(), "User2")));
        WatchingSession s3 = new WatchingSession("s3", new Watcher(createMockUserDto(UUID.randomUUID(), "User3")));
        given(valueOps.multiGet(anyList())).willReturn(Arrays.asList(s2, s3));

        // when
        // DESCENDING 정렬 시 s1(idAfter)을 찾아서 그 뒤의 데이터를 가져오는지 테스트
        CursorResponseWatchingSessionDto response = watchTogetherService.getWatcherList(
                contentUuidStr, null, null, idAfter, 2, "DESCENDING", "createdAt"
        );

        // then
        // s1은 dropWhile에 의해 제거되고 s2, s3만 남아야 함 (limit 2)
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).watcher().getName()).isEqualTo("User2");
        verify(zSetOps).reverseRange(eq("room:" + contentUuidStr + ":watchers"), eq(0L), eq(-1L));
    }

    @Test
    @DisplayName("시청자 목록 조회: 컨텐츠가 없으면 예외가 발생한다.")
    void getWatcherList_NoContent() {
        // given
        String contentUuidStr = UUID.randomUUID().toString();
        given(contentCommandRepository.findByUuid(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> watchTogetherService.getWatcherList(
                contentUuidStr, "", "", "", 10, "DESC", "createdAt"
        )).isInstanceOf(ContentException.class);
    }
}