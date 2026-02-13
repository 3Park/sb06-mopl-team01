package org.example.mopl.directmessage;

import jakarta.persistence.EntityManager;
import org.example.mopl.directmessage.dto.data.ConversationDto;
import org.example.mopl.directmessage.dto.data.DirectMessageDto;
import org.example.mopl.directmessage.dto.request.ConversationListRequest;
import org.example.mopl.directmessage.dto.request.DirectMessageListRequest;
import org.example.mopl.directmessage.dto.response.CursorResponseConversationDto;
import org.example.mopl.directmessage.dto.response.CursorResponseDirectMessageDto;
import org.example.mopl.directmessage.entity.Conversation;
import org.example.mopl.directmessage.entity.DirectMessage;
import org.example.mopl.directmessage.enums.SortBy;
import org.example.mopl.directmessage.exception.ConversationForbiddenException;
import org.example.mopl.directmessage.exception.ConversationNotFoundException;
import org.example.mopl.directmessage.repository.ConversationRepository;
import org.example.mopl.directmessage.repository.DirectMessageRepository;
import org.example.mopl.directmessage.service.DirectMessageService;
import org.example.mopl.event.message.DmMessageReceivedKafkaEvent;
import org.example.mopl.profile.entity.Profile;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.UserRepository;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test-dm")
@Testcontainers
public class DirectMessageIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private EntityManager em;
    @Autowired
    private DirectMessageRepository directMessageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private DirectMessageService directMessageService;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;
    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    private User creator;
    private User joiner;
    private User other;
    private String content;


    @BeforeEach
    void setUp() {
        creator = createUserWithProfile("aaa@aaa.com", "유저 A");
        joiner = createUserWithProfile("bbb@bbb.com", "유저 B");
        other = createUserWithProfile("ccc@ccc.com", "유저 C");
        content = "테스트 메시지입니다.";
    }


    @Test
    @DisplayName("메시지 저장 성공")
    void saveMessage_Success(){
        // given
        Conversation conversation = createConversation(creator, joiner);

        // when
        directMessageService.saveAndSendMessage(conversation.getUuid(), creator.getUuid(), content);

        // then
        List<DirectMessage> messages = directMessageRepository.findAll();

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).isEqualTo(content);
        assertThat(messages.get(0).getSenderId()).isEqualTo(creator.getId());

        verify(messagingTemplate, times(1))
                .convertAndSend(anyString(), any(DirectMessageDto.class));
    }
    @Test
    @DisplayName("메시지 저장 실패 - 대화 참여자 아님")
    void saveMessage_NotParticipant_Fail() {
        // given
        Conversation conversation = createConversation(creator, joiner);

        // when & then
        assertThatThrownBy(() -> directMessageService
                .saveAndSendMessage(conversation.getUuid(), other.getUuid(), content))
                .isInstanceOf(ConversationForbiddenException.class);

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(DirectMessageDto.class));
        verify(eventPublisher, never()).publishEvent(any(DmMessageReceivedKafkaEvent.class));
    }
    @Test
    @DisplayName("메시지 저장 실패 - 해당 대화 부재")
    void saveMessage_NotFoundConversation_Fail() {
        // given
        UUID invalidConversationId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> directMessageService
                .saveAndSendMessage(invalidConversationId, creator.getUuid(), content))
                .isInstanceOf(ConversationNotFoundException.class);

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(DirectMessageDto.class));
    }

    @Test
    @DisplayName("대화 생성 성공 - 신규")
    void create_NewConversation_Success() {
        // when
        ConversationDto result = directMessageService.create(creator.getUuid(), joiner.getUuid());

        // then
        List<Conversation> conversations = conversationRepository.findAll();

        assertThat(conversations.size()).isEqualTo(1);
        assertThat(conversations.get(0).getCreatorId()).isEqualTo(creator.getId());
        assertThat(conversations.get(0).getJoinId()).isEqualTo(joiner.getId());
    }
    @Test
    @DisplayName("대화 생성 성공 - 기존 대화 존재")
    void create_ExistingConversation_Success() {
        // given
        Conversation existingConversation = createConversation(creator, joiner);;

        // when
        ConversationDto result = directMessageService.create(creator.getUuid(), joiner.getUuid());

        // then
        List<Conversation> conversations = conversationRepository.findAll();

        assertThat(result.id()).isEqualTo(existingConversation.getUuid());
        assertThat(conversations).hasSize(1);
        assertThat(conversations.get(0).getUuid()).isEqualTo(existingConversation.getUuid());
    }

    @Test
    @DisplayName("DM 읽음 처리 성공")
    void markAsRead_Success() {
        // given
        Conversation conversation = createConversation(creator, joiner);

        DirectMessage firstMessage = saveMessage( // receiver == creator
                conversation, joiner.getId(), creator.getId(), content);
        DirectMessage secondMessage =saveMessage( // receiver == joiner
                conversation, creator.getId(), joiner.getId(), content);
        DirectMessage targetMessage = saveMessage( // receiver == creator, 기준 메시지
                conversation, joiner.getId(), creator.getId(), content);
        DirectMessage lastMessage = saveMessage( // receiver == creator
                conversation, joiner.getId(), creator.getId(), content);


        // when
        directMessageService.markAsRead(conversation.getUuid(), targetMessage.getUuid(), creator.getUuid());
        em.flush();
        em.clear();
        firstMessage = directMessageRepository.findById(firstMessage.getId()).orElse(null);
        secondMessage = directMessageRepository.findById(secondMessage.getId()).orElse(null);
        targetMessage = directMessageRepository.findById(targetMessage.getId()).orElse(null);
        lastMessage = directMessageRepository.findById(lastMessage.getId()).orElse(null);

        // then
        assertThat(firstMessage.isRead()).isTrue();
        assertThat(secondMessage.isRead()).isFalse();
        assertThat(targetMessage.isRead()).isTrue();
        assertThat(lastMessage.isRead()).isFalse();
    }

    @Test
    @DisplayName("대화 목록 조회 성공 - 첫 페이지")
    void getConversations_FirstPage_Success() {
        // given
        Conversation firstConversation = createConversation(creator, joiner);
        Conversation secondConversation = createConversation(creator, joiner);
        Conversation thirdConversation = createConversation(creator, joiner);

        saveMessage(firstConversation, joiner.getId(), creator.getId(), "테스트입니다.");
        saveMessage(firstConversation, joiner.getId(), creator.getId(), "마지막 메시지.");
        saveMessage(secondConversation, joiner.getId(), creator.getId(), "테스트 아닙니다.");
        saveMessage(thirdConversation, joiner.getId(), creator.getId(), "키워드 미포함.");


        // when
        ConversationListRequest request = ConversationListRequest.builder()
                .limit(1).keywordLike("테스트")
                .sortDirection(SortDirection.ASCENDING).sortBy(SortBy.createdAt).build();
        CursorResponseConversationDto result = directMessageService.getConversations(
                creator.getUuid(), request
        );

        // then
        DirectMessageDto lastMessageInFirstConversation = result.data().stream()
                .filter(c -> c.id().equals(firstConversation.getUuid()))
                .map(ConversationDto::lastestMessage).findFirst().orElse(null);

        assertThat(result.data().size()).isEqualTo(1);
        assertThat(lastMessageInFirstConversation.content()).isEqualTo("마지막 메시지.");
        assertThat(result.hasNext()).isTrue();
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.nextIdAfter()).isEqualTo(firstConversation.getUuid());
    }
    @Test
    @DisplayName("대화 목록 조회 성공 - 페이지네이션")
    void getConversations_Pagination_Success() {
        // given
        Conversation firstConversation = createConversation(creator, joiner);
        Conversation secondConversation = createConversation(creator, joiner);
        Conversation thirdConversation = createConversation(creator, joiner);

        saveMessage(firstConversation, joiner.getId(), creator.getId(), "테스트입니다.");
        saveMessage(firstConversation, joiner.getId(), creator.getId(), "마지막 메시지.");
        saveMessage(secondConversation, joiner.getId(), creator.getId(), "테스트 아닙니다.");
        saveMessage(thirdConversation, joiner.getId(), creator.getId(), "키워드 미포함.");


        // when
        ConversationListRequest request = ConversationListRequest.builder()
                .limit(1).keywordLike("테스트").idAfter(firstConversation.getUuid())
                .sortDirection(SortDirection.ASCENDING).sortBy(SortBy.createdAt).build();
        CursorResponseConversationDto result = directMessageService.getConversations(
                creator.getUuid(), request
        );

        // then
        assertThat(result.data().size()).isEqualTo(1);
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextIdAfter()).isEqualTo(secondConversation.getUuid());
    }

    @Test
    @DisplayName("DM 목록 조회 성공 - 첫 페이지")
    void getDirectMessages_FirstPage_Success() {
        // given
        Conversation conversation = createConversation(creator, joiner);
        DirectMessage firstMessage = saveMessage(conversation, creator.getId(), joiner.getId(), content);
        DirectMessage secondMessage = saveMessage(conversation, joiner.getId(), creator.getId(), content);

        // when
        DirectMessageListRequest request = DirectMessageListRequest.builder()
                .limit(1).sortDirection(SortDirection.ASCENDING).sortBy(SortBy.createdAt).build();
        CursorResponseDirectMessageDto result = directMessageService.getDirectMessages(
                creator.getUuid(), conversation.getUuid(), request
        );

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.data().size()).isEqualTo(1);
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.data().get(0).id()).isEqualTo(firstMessage.getUuid());
        assertThat(result.nextIdAfter()).isEqualTo(firstMessage.getUuid());
    }
    @Test
    @DisplayName("DM 목록 조회 성공 - 페이지네이션")
    void getDirectMessages_Pagination_Success() {
        // given
        Conversation conversation = createConversation(creator, joiner);
        DirectMessage firstMessage = saveMessage(conversation, creator.getId(), joiner.getId(), content);
        DirectMessage secondMessage = saveMessage(conversation, joiner.getId(), creator.getId(), content);

        // when
        DirectMessageListRequest request = DirectMessageListRequest.builder()
                .idAfter(firstMessage.getUuid()).limit(1)
                .sortDirection(SortDirection.ASCENDING).sortBy(SortBy.createdAt).build();
        CursorResponseDirectMessageDto result = directMessageService.getDirectMessages(
                creator.getUuid(), conversation.getUuid(), request
        );

        // then
        assertThat(result.hasNext()).isFalse();
        assertThat(result.data().size()).isEqualTo(1);
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.data().get(0).id()).isEqualTo(secondMessage.getUuid());
        assertThat(result.nextIdAfter()).isEqualTo(secondMessage.getUuid());
    }
    @Test
    @DisplayName("DM 목록 조회 실패 - 대화 참여자 아님")
    void getDirectMessages_NotParticipant_Fail() {
        // given
        Conversation conversation = createConversation(creator, joiner);

        // when & then
        assertThatThrownBy(() -> directMessageService.getDirectMessages(
                other.getUuid(), conversation.getUuid(), DirectMessageListRequest.builder().build()
        )).isInstanceOf(ConversationForbiddenException.class);
    }


    // === helper method===
    private User createUserWithProfile(String email, String name) {
        User user = User.builder().email(email).password("password123!").build();
        Profile profile = Profile.builder().user(user).name(name).build();
        user.setProfile(profile);
        return userRepository.saveAndFlush(user);
    }
    private DirectMessage saveMessage(Conversation conversation, Long senderId,
                                      Long receiverId, String content) {
        DirectMessage message = DirectMessage.of(
                conversation, senderId, receiverId, content);
        return directMessageRepository.save(message);
    }
    private Conversation createConversation(User creator, User joiner) {
        Conversation conversation = Conversation.of(creator.getId(), joiner.getId());
        return conversationRepository.save(conversation);
    }
}
