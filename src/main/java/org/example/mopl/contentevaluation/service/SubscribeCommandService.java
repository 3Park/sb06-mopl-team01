package org.example.mopl.contentevaluation.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.exception.ContentErrorCode;
import org.example.mopl.content.exception.ContentException;
import org.example.mopl.contentevaluation.entity.Playlist;
import org.example.mopl.contentevaluation.entity.Subscribe;
import org.example.mopl.contentevaluation.event.SubscribeCountEvent;
import org.example.mopl.contentevaluation.exception.NoSuchPlaylistException;
import org.example.mopl.contentevaluation.repository.PlaylistQueryRepository;
import org.example.mopl.contentevaluation.repository.SubscribeCommandRepository;
import org.example.mopl.event.message.PlaylistSubscriptionCreatedKafkaEvent;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscribeCommandService {

  private final PlaylistQueryRepository playlistQueryRepository;
  private final SubscribeCommandRepository subscribeCommandRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void subscribePlaylist(String email, UUID playlistId) {

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_CONTENT));

    Playlist playlist = playlistQueryRepository.findByUuid(playlistId)
        .orElseThrow(() -> new NoSuchPlaylistException(playlistId));

    subscribeCommandRepository.save(
        Subscribe.of(user, playlist)
    );

    eventPublisher.publishEvent(
        SubscribeCountEvent.IncreaseSubscribeCountEvent.of(
            playlist.getId(),
            playlist.getUuid()
        )
    );

    // 플레이리스트 소유자에게 알림 전송 이벤트 발행
    eventPublisher.publishEvent(
      PlaylistSubscriptionCreatedKafkaEvent.of(
          playlist.getUser().getUuid(),
          user.getProfile().getName(),
          playlist.getTitle(),
          playlist.getDescription()
      )
    );

  }

  @Transactional
  public void unsubscribePlaylist(String email, UUID playlistId) {

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_AUTHOR));

    Playlist playlist = playlistQueryRepository.findByUuid(playlistId)
        .orElseThrow(() -> new NoSuchPlaylistException(playlistId));

    subscribeCommandRepository.deleteByUser_IdAndPlaylist_Id(user.getId(), playlist.getId());

    eventPublisher.publishEvent(
        SubscribeCountEvent.DecreaseSubscribeCountEvent.of(
            playlist.getId(),
            playlist.getUuid()
        )
    );

  }

}
