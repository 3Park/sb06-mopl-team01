package org.example.mopl.watchtogether;

import org.example.mopl.auth.jwt.JwtTokenProvider;
import org.example.mopl.auth.service.MailService;
import org.example.mopl.content.batch.service.TheSportsDbBatchService;
import org.example.mopl.content.batch.service.TmDbBatchService;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.content.s3.ContentS3Client;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.dto.ContentChatSendRequest;
import org.example.mopl.watchtogether.dto.CursorResponseWatchingSessionDto;
import org.example.mopl.watchtogether.model.Watcher;
import org.example.mopl.watchtogether.service.BasicWatchTogetherService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest(
        properties = {
                "spring.data.redis.host=localhost",
                "spring.data.redis.port=6379",
                "spring.data.redis.connect-timeout=60s",
                "spring.data.redis.lettuce.pool.max-active=8",
                "spring.data.redis.lettuce.pool.max-idle=8",
                "spring.data.redis.lettuce.pool.min-idle=0",
                "spring.data.redis.lettuce.pool.max-wait=60s",
                "spring.data.redis.lettuce.command-timeout=60s",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.cloud.aws.s3.bucket=test-bucket",
                "spring.cloud.aws.region.static=ap-northeast-2", // 지역 설정도 같이 넣어주는 것이 안전합니다.
                "spring.cloud.aws.credentials.access-key=dummy",
                "spring.cloud.aws.credentials.secret-key=dummy",
                "EMAIL_ADDRESS=test@test.com",
                "EMAIL_PASSWORD=testpassword",
                "spring.kafka.bootstrap-servers=localhost:9092",
                "spring.kafka.consumer.group-id=test-group",
                "content.api.thesportsdb.key=dummy-api-key",
                "content.api.thesportsdb.url=https://dummy-url.com",
                "content.api.tmdb.key=dummy-tmdb-key",
                "content.api.tmdb.url=https://dummy-url.com",
                "spring.cloud.aws.s3.presigned-url-expiration=60000"
        }
)
@Transactional
@ActiveProfiles("test")
class WatchTogetherServiceIntegrationTest {

    @Autowired
    private BasicWatchTogetherService watchTogetherService;

    @Autowired
    private ContentCommandRepository contentCommandRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private EntityManager em;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MailService mailService;

    @MockitoBean
    private TheSportsDbBatchService theSportsDbBatchService;

    @MockitoBean
    private TmDbBatchService tmDbBatchService;

    @MockitoBean
    private ContentS3Client contentS3Client;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    private Content savedContent;
    private String contentUuidStr;

    @BeforeEach
    void setUp() {
        // [중요] 저장 후 영속성 컨텍스트 초기화하여 실제 DB 상태 반영
        Content content = Content.of("MOVIE", "Integration Test", "Desc", "url");
        this.savedContent = contentCommandRepository.saveAndFlush(content);

        // 만약 엔티티에서 UUID를 자동생성하지 않는다면 강제 주입
        if (savedContent.getUuid() == null) {
            ReflectionTestUtils.setField(savedContent, "uuid", UUID.randomUUID());
            contentCommandRepository.saveAndFlush(savedContent);
        }

        this.contentUuidStr = savedContent.getUuid().toString();
        em.clear();
    }

    @AfterEach
    void tearDown() {
        Objects.requireNonNull(stringRedisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    @Test
    @DisplayName("중복 로그인 테스트: 동일 유저가 새 세션으로 접속하면 기존 세션은 삭제된다.")
    void duplicateLogin_ShouldRemoveOldSession() {
        // given
        UUID userId = UUID.randomUUID();
        UserDto user = createMockUser(userId, "Tester");
        String oldSessionId = "session-1";
        String newSessionId = "session-2";

        // 1. 첫 번째 로그인
        watchTogetherService.addUserToRoom(user, contentUuidStr, oldSessionId);
        assertThat(watchTogetherService.getWatcherCount(contentUuidStr)).isEqualTo(1);

        // when
        // 2. 동일 유저가 다른 세션으로 로그인
        watchTogetherService.addUserToRoom(user, contentUuidStr, newSessionId);

        // then
        // 인원수는 여전히 1명이어야 함 (기존 세션이 삭제되었으므로)
        assertThat(watchTogetherService.getWatcherCount(contentUuidStr)).isEqualTo(1);

        // Redis에 이전 세션 데이터가 없어야 함
        assertThat(stringRedisTemplate.hasKey("session:" + oldSessionId + ":room")).isFalse();
        // 현재 세션 데이터는 있어야 함
        assertThat(stringRedisTemplate.hasKey("session:" + newSessionId + ":room")).isTrue();
    }

    @Test
    @DisplayName("통합 테스트: 방 퇴장 시 인원이 0명이면 active_rooms에서 제거된다.")
    void removeUser_ShouldCleanUpActiveRooms_WhenEmpty() {
        // given
        String sessionId = UUID.randomUUID().toString();
        UserDto user = createMockUser(UUID.randomUUID(), "Leaver");
        watchTogetherService.addUserToRoom(user, contentUuidStr, sessionId);
        assertThat(stringRedisTemplate.opsForSet().isMember("active_rooms", contentUuidStr)).isTrue();

        // when
        watchTogetherService.removeUserFromRoom(sessionId);

        // then
        assertThat(watchTogetherService.getWatcherCount(contentUuidStr)).isZero();
        assertThat(stringRedisTemplate.opsForSet().isMember("active_rooms", contentUuidStr)).isFalse();
    }

    @Test
    @DisplayName("통합 테스트: 커서 기반 페이지네이션 (DESCENDING)")
    void getWatcherList_Pagination_Integration() {
        // given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        // 순차 입장 (시간차를 확실히 두어 score 차이 발생시킴)
        watchTogetherService.addUserToRoom(createMockUser(id1, "User1"), contentUuidStr, "s1");
        sleep(100); // 10ms -> 100ms로 증가
        watchTogetherService.addUserToRoom(createMockUser(id2, "User2"), contentUuidStr, "s2");
        sleep(100);
        watchTogetherService.addUserToRoom(createMockUser(id3, "User3"), contentUuidStr, "s3");

        // when
        // 정렬: DESCENDING (최신순: s3 -> s2 -> s1)
        // Cursor: idAfter = id3 (s3)
        // limit: 2
        CursorResponseWatchingSessionDto response = watchTogetherService.getWatcherList(
                contentUuidStr, null, null, id3.toString(), 2, "DESCENDING", "createdAt"
        );

        // then
        // 서비스 로직에서 limit + 1을 가져오므로 3개가 반환되는 것이 맞다면:
        assertThat(response.data()).hasSize(2);

        // 정렬 순서 검증 (최신순)
        assertThat(response.data().get(0).id()).isEqualTo("s3");
        assertThat(response.data().get(1).id()).isEqualTo("s2");
    }

    @Test
    @DisplayName("통합 테스트: 채팅 메시지 전송 검증")
    void sendMessageToRoom_Integration() {
        // given
        UserDto user = createMockUser(UUID.randomUUID(), "Chatter");
        watchTogetherService.addUserToRoom(user, contentUuidStr, "chat-session");

        ContentChatSendRequest request = new ContentChatSendRequest("Hello!");
        Watcher watcher = new Watcher(user);

        // when
        watchTogetherService.sendMessageToRoom(contentUuidStr, request, watcher);

        // then
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/sub/contents/" + contentUuidStr + "/chat"),
                any(Object.class)
        );
    }

    private UserDto createMockUser(UUID id, String name) {
        UserDto userDto = mock(UserDto.class);
        given(userDto.getId()).willReturn(id);
        given(userDto.getName()).willReturn(name);
        return userDto;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}