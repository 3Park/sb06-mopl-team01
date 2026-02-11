package org.example.mopl.watchtogether;

import jakarta.persistence.EntityManager;
import org.example.mopl.auth.jwt.JwtTokenProvider;
import org.example.mopl.auth.service.MailService;
import org.example.mopl.content.batch.service.TheSportsDbBatchService;
import org.example.mopl.content.batch.service.TmDbBatchService;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.repository.ContentCommandRepository;
import org.example.mopl.content.s3.ContentS3Client;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.watchtogether.dto.ContentChatDto;
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

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        properties = {
                //[Redis] 로컬 Redis 연결
                "spring.data.redis.host=localhost",
                "spring.data.redis.port=6379",
                "spring.data.redis.connect-timeout=60s",
                "spring.data.redis.lettuce.pool.max-active=8",
                "spring.data.redis.lettuce.pool.max-idle=8",
                "spring.data.redis.lettuce.pool.min-idle=0",
                "spring.data.redis.lettuce.pool.max-wait=60s",

                // [DB] H2 테스트용
                "spring.jpa.hibernate.ddl-auto=create-drop",

                // [기타] 외부 의존성 더미 설정
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
        Content content = Content.of("MOVIE", "Common Integration Title", "Desc", "url");

        this.savedContent = contentCommandRepository.saveAndFlush(content);

        UUID actualUuid = content.getUuid();

        this.contentUuidStr = actualUuid.toString();

        contentCommandRepository.findByUuid(actualUuid)
                .orElseThrow(() -> new RuntimeException("DB 저장 실패. 조회 불가 UUID: " + actualUuid));
    }

    @AfterEach
    void tearDown() {
        // 요청하신 대로 Objects.requireNonNull을 사용하는 방식으로 변경되었습니다.
        Objects.requireNonNull(stringRedisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    @Test
    @DisplayName("통합 테스트: DB에 저장된 컨텐츠로 방을 생성하고 유저가 입장한다.")
    void addUserToRoom_Integration() {
        String sessionId = UUID.randomUUID().toString();
        UserDto mockUser = createMockUser(UUID.randomUUID(), "IntegrationTester");

        watchTogetherService.addUserToRoom(mockUser, contentUuidStr, sessionId);

        assertThat(watchTogetherService.getWatcherCount(contentUuidStr)).isEqualTo(1);
        verify(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    @DisplayName("통합 테스트: 유저가 방에서 나가면 방이 삭제된다.")
    void removeUserFromRoom_Integration() {
        String sessionId = UUID.randomUUID().toString();
        UserDto mockUser = createMockUser(UUID.randomUUID(), "Leaver");

        watchTogetherService.addUserToRoom(mockUser, contentUuidStr, sessionId);
        watchTogetherService.removeUserFromRoom(sessionId);

        assertThat(watchTogetherService.getWatchingRooms()).doesNotContainKey(savedContent.getId());
        assertThat(watchTogetherService.getWatcherCount(contentUuidStr)).isZero();
    }

    @Test
    @DisplayName("통합 테스트: 커서 기반 페이지네이션이 동작한다.")
    void getWatcherList_Pagination_Integration() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        watchTogetherService.addUserToRoom(createMockUser(id1, "User1"), contentUuidStr, UUID.randomUUID().toString());
        sleep(10);
        watchTogetherService.addUserToRoom(createMockUser(id2, "User2"), contentUuidStr, UUID.randomUUID().toString());
        sleep(10);
        watchTogetherService.addUserToRoom(createMockUser(id3, "User3"), contentUuidStr, UUID.randomUUID().toString());

        CursorResponseWatchingSessionDto response = watchTogetherService.getWatcherList(
                contentUuidStr, null, "User3", id3.toString(), 10, "DESCENDING", "createdAt"
        );

        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).watcher().getName()).isEqualTo("User2");
        assertThat(response.data().get(1).watcher().getName()).isEqualTo("User1");
    }

    @Test
    @DisplayName("통합 테스트: 채팅 메시지가 올바른 경로로 전송된다.")
    void sendMessageToRoom_Integration() {
        UserDto mockUser = createMockUser(UUID.randomUUID(), "Chatter");
        watchTogetherService.addUserToRoom(mockUser, contentUuidStr, UUID.randomUUID().toString());

        ContentChatSendRequest request = new ContentChatSendRequest("Hello World");
        Watcher watcher = new Watcher(mockUser);

        watchTogetherService.sendMessageToRoom(contentUuidStr, request, watcher);

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ContentChatDto> payloadCaptor = ArgumentCaptor.forClass(ContentChatDto.class);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        assertThat(destinationCaptor.getValue()).isEqualTo("/sub/contents/" + contentUuidStr + "/chat");
        assertThat(payloadCaptor.getValue().content()).isEqualTo("Hello World");
    }

    private UserDto createMockUser(UUID id, String name) {
        UserDto userDto = org.mockito.Mockito.mock(UserDto.class);
        given(userDto.getId()).willReturn(id);
        given(userDto.getName()).willReturn(name);
        return userDto;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}