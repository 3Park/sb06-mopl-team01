package org.example.mopl.watchtogether;

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
import org.example.mopl.watchtogether.dto.WatchingSessionDto;
import org.example.mopl.watchtogether.model.Watcher;
import org.example.mopl.watchtogether.service.BasicWatchTogetherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        properties = {
                // [Redis] 더미 설정
                "spring.data.redis.host=localhost",
                "spring.data.redis.port=6379",

                // [DB] H2 테스트용 스키마 생성
                "spring.jpa.hibernate.ddl-auto=create-drop",

                // [Mail] MailService 의존성 해결
                "EMAIL_ADDRESS=test@test.com",
                "EMAIL_PASSWORD=testpassword",

                // [Kafka] KafkaConfig 의존성 해결
                "spring.kafka.bootstrap-servers=localhost:9092",
                "spring.kafka.consumer.group-id=test-group",

                // [External API] 크롤러 클라이언트 의존성 해결
                "content.api.thesportsdb.key=dummy-api-key",
                "content.api.thesportsdb.url=https://dummy-url.com",
                "content.api.tmdb.key=dummy-tmdb-key",
                "content.api.tmdb.url=https://dummy-url.com",

                // [AWS S3] ContentS3Client 의존성 해결 (밀리초)
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

    // [WebSocket] 인터페이스가 아닌 구체 클래스로 Mocking (Bean 타입 불일치 방지)
    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    // [Mock Beans] 서비스 로직과 무관한 외부 의존성 차단
    @MockitoBean(name = "refreshTokenRedisConnectionFactory")
    private RedisConnectionFactory refreshTokenRedisConnectionFactory;

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

    @Test
    @DisplayName("통합 테스트: DB에 저장된 컨텐츠로 방을 생성하고 유저가 입장한다.")
    void addUserToRoom_Integration() {
        // given
        Content content = Content.of("MOVIE", "Real DB Title", "Desc", "url");
        Content savedContent = contentCommandRepository.save(content);

        String contentUuidStr = savedContent.getUuid().toString();
        String sessionId = UUID.randomUUID().toString();

        UserDto mockUser = org.mockito.Mockito.mock(UserDto.class);
        given(mockUser.getId()).willReturn(UUID.randomUUID());
        given(mockUser.getName()).willReturn("IntegrationTester");

        // when
        watchTogetherService.addUserToRoom(mockUser, contentUuidStr, sessionId);

        // then
        assertThat(watchTogetherService.getWatcherCount(contentUuidStr)).isEqualTo(1);
        verify(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    @DisplayName("통합 테스트: 유저가 방에서 나가면 방이 삭제된다.")
    void removeUserFromRoom_Integration() {
        // given
        Content content = Content.of("MOVIE", "Delete Test", "Desc", "url");
        Content savedContent = contentCommandRepository.save(content);
        String contentUuidStr = savedContent.getUuid().toString();

        String sessionId = UUID.randomUUID().toString();

        UserDto mockUser = org.mockito.Mockito.mock(UserDto.class);
        given(mockUser.getId()).willReturn(UUID.randomUUID());
        given(mockUser.getName()).willReturn("Leaver");

        watchTogetherService.addUserToRoom(mockUser, contentUuidStr, sessionId);

        // when
        watchTogetherService.removeUserFromRoom(sessionId);

        // then
        assertThat(watchTogetherService.getWatchingRooms())
                .doesNotContainKey(savedContent.getId());
    }

    @Test
    @DisplayName("통합 테스트: 커서 기반 페이지네이션(필터링)이 동작한다.")
    void getWatcherList_Pagination_Integration() {
        // given
        Content content = Content.of("MOVIE", "Pagination Test", "Desc", "url");
        Content savedContent = contentCommandRepository.save(content);
        String contentUuidStr = savedContent.getUuid().toString();

        // 3명의 유저 준비
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        UserDto user1 = org.mockito.Mockito.mock(UserDto.class);
        given(user1.getId()).willReturn(id1);
        given(user1.getName()).willReturn("User1");

        UserDto user2 = org.mockito.Mockito.mock(UserDto.class);
        given(user2.getId()).willReturn(id2);
        given(user2.getName()).willReturn("User2");

        UserDto user3 = org.mockito.Mockito.mock(UserDto.class);
        given(user3.getId()).willReturn(id3);
        given(user3.getName()).willReturn("User3");

        // 순차 입장 (User1 -> User2 -> User3)
        watchTogetherService.addUserToRoom(user1, contentUuidStr, UUID.randomUUID().toString());
        watchTogetherService.addUserToRoom(user2, contentUuidStr, UUID.randomUUID().toString());
        watchTogetherService.addUserToRoom(user3, contentUuidStr, UUID.randomUUID().toString());

        // when
        // 1. DESCENDING 정렬: [User3, User2, User1] 순서임
        // 2. Cursor: "User3" (가장 최신 사람)
        // 3. Expectation: User3는 건너뛰고, 그 다음인 "User2"부터 나와야 함
        CursorResponseWatchingSessionDto response = watchTogetherService.getWatcherList(
                contentUuidStr,
                null,
                "User3",
                id3.toString(),
                10,
                "DESCENDING",
                "createdAt"
        );

        // then
        assertThat(response.data()).hasSize(2); // User2, User1
        WatchingSessionDto firstResult = response.data().get(0);

        assertThat(firstResult.watcher().getName()).isEqualTo("User2");
        assertThat(response.data().get(1).watcher().getName()).isEqualTo("User1");
    }

    @Test
    @DisplayName("통합 테스트: 채팅 메시지가 올바른 경로와 데이터로 전송된다.")
    void sendMessageToRoom_Integration() {
        // given
        Content content = Content.of("MOVIE", "Chat Test Title", "Desc", "url");
        Content savedContent = contentCommandRepository.save(content);
        String contentUuidStr = savedContent.getUuid().toString();

        UserDto mockUser = org.mockito.Mockito.mock(UserDto.class);
        UUID userId = UUID.randomUUID();
        given(mockUser.getId()).willReturn(userId);
        given(mockUser.getName()).willReturn("Chatter");

        watchTogetherService.addUserToRoom(mockUser, contentUuidStr, UUID.randomUUID().toString());

        String chatMessage = "안녕하세요! 반갑습니다.";
        ContentChatSendRequest request = new ContentChatSendRequest(chatMessage);
        Watcher watcher = new Watcher(mockUser);

        // when
        watchTogetherService.sendMessageToRoom(contentUuidStr, request, watcher);

        // then
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ContentChatDto> payloadCaptor = ArgumentCaptor.forClass(ContentChatDto.class);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        assertThat(destinationCaptor.getValue()).isEqualTo("/sub/contents/" + contentUuidStr + "/chat");

        ContentChatDto sentDto = payloadCaptor.getValue();
        assertThat(sentDto.content()).isEqualTo(chatMessage);
        assertThat(sentDto.sender().getName()).isEqualTo("Chatter");
        assertThat(sentDto.sender().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("통합 테스트: 존재하지 않는 방 조회 시 0명 반환 (Null Safety)")
    void getWatcherCount_Safe_Integration() {
        // given
        String randomContentId = UUID.randomUUID().toString();

        // when
        long count = watchTogetherService.getWatcherCount(randomContentId);

        // then
        assertThat(count).isEqualTo(0L);
    }
}