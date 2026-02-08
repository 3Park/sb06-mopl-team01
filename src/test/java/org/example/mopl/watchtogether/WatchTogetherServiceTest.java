package org.example.mopl.watchtogether;

import org.example.mopl.content.entity.Content;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.dto.CursorResponseWatchingSessionDto;
import org.example.mopl.watchtogether.service.BasicWatchTogetherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WatchTogetherServiceTest {
    // 단위테스트 입니다.

    @InjectMocks
    private BasicWatchTogetherService watchTogetherService;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private ContentCommandRepository contentCommandRepository;

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
    @DisplayName("방 입장: 방이 없을 경우 새로 생성하고 유저를 추가한다.")
    void addUserToRoom_createRoom() {
        // given
        String contentUuidStr = UUID.randomUUID().toString();
        Long contentId = 1L;
        String sessionId = UUID.randomUUID().toString();

        UUID userId = UUID.randomUUID();
        UserDto userDto = createMockUserDto(userId, "Tester");

        Content content = createMockContent(contentId, UUID.fromString(contentUuidStr));

        given(contentCommandRepository.findByUuid(UUID.fromString(contentUuidStr)))
                .willReturn(Optional.of(content));

        // when
        watchTogetherService.addUserToRoom(userDto, contentUuidStr, sessionId);

        // then
        assertThat(watchTogetherService.getWatcherCount(contentUuidStr)).isEqualTo(1);
        verify(messagingTemplate, times(1))
                .convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    @DisplayName("시청자 수 조회: ID가 Null이거나 없으면 0을 반환한다 (Null Safety).")
    void getWatcherCount_NullSafe() {
        // given
        String nonExistentId = UUID.randomUUID().toString();

        // when & then
        assertThat(watchTogetherService.getWatcherCount(null)).isZero();
        assertThat(watchTogetherService.getWatcherCount("")).isZero();
        assertThat(watchTogetherService.getWatcherCount(nonExistentId)).isZero();
    }

    @Test
    @DisplayName("방 퇴장: 유저가 나가고 방에 사람이 없으면 방이 삭제된다.")
    void removeUserFromRoom_deleteRoom() {
        // given
        String contentUuidStr = UUID.randomUUID().toString();
        Long contentId = 1L;
        String sessionId = UUID.randomUUID().toString();

        UUID userId = UUID.randomUUID();
        UserDto userDto = createMockUserDto(userId, "Tester");

        Content content = createMockContent(contentId, UUID.fromString(contentUuidStr));
        given(contentCommandRepository.findByUuid(any())).willReturn(Optional.of(content));

        watchTogetherService.addUserToRoom(userDto, contentUuidStr, sessionId);

        // when
        watchTogetherService.removeUserFromRoom(sessionId);

        // then
        assertThat(watchTogetherService.getWatchingRooms()).doesNotContainKey(contentId);
        verify(messagingTemplate, times(2))
                .convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    @DisplayName("시청자 목록 조회: 정렬(ASC) 및 커서 기반 필터링이 동작한다.")
    void getWatcherList_SortedAndFiltered() {
        // given
        String contentUuidStr = UUID.randomUUID().toString();
        Long contentId = 1L;
        Content content = createMockContent(contentId, UUID.fromString(contentUuidStr));
        given(contentCommandRepository.findByUuid(any())).willReturn(Optional.of(content));

        // 3명의 유저 생성
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        UserDto user1 = createMockUserDto(id1, "User1");
        UserDto user2 = createMockUserDto(id2, "User2");
        UserDto user3 = createMockUserDto(id3, "User3");

        String s1 = UUID.randomUUID().toString();
        String s2 = UUID.randomUUID().toString();
        String s3 = UUID.randomUUID().toString();

        // 순서대로 입장 (CreatedAt: User1 < User2 < User3)
        watchTogetherService.addUserToRoom(user1, contentUuidStr, s1);
        watchTogetherService.addUserToRoom(user2, contentUuidStr, s2);
        watchTogetherService.addUserToRoom(user3, contentUuidStr, s3);

        // when: User1을 커서로 주고, 그 다음 목록을 조회 (ASCENDING)
        // User1 다음인 User2부터 나와야 함
        CursorResponseWatchingSessionDto response = watchTogetherService.getWatcherList(
                contentUuidStr,
                null,
                "User1",        // cursor (name)
                id1.toString(), // idAfter (userId)
                10,
                "ASCENDING",
                "createdAt"
        );

        // then
        // User1은 dropWhile에 의해 제외되고, User2와 User3가 남아야 함
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).watcher().getName()).isEqualTo("User2");
        assertThat(response.data().get(1).watcher().getName()).isEqualTo("User3");
    }

    @Test
    @DisplayName("시청자 목록 조회: 방이 없으면 예외가 발생한다.")
    void getWatcherList_NoRoom() {
        // given
        String contentUuidStr = UUID.randomUUID().toString();

        // when & then
        assertThatThrownBy(() -> watchTogetherService.getWatcherList(
                contentUuidStr, "", "", "", 10, "DESC", "createdAt"
        )).isInstanceOf(NoSuchContentException.class);
    }
}
