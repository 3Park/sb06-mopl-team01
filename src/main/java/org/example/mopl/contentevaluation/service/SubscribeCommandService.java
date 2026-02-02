package org.example.mopl.contentevaluation.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.contentevaluation.entity.Playlist;
import org.example.mopl.contentevaluation.entity.Subscribe;
import org.example.mopl.contentevaluation.event.SubscribeCountEvent;
import org.example.mopl.contentevaluation.exception.NoSuchPlaylistException;
import org.example.mopl.contentevaluation.repository.PlaylistQueryRepository;
import org.example.mopl.contentevaluation.repository.SubscribeCommandRepository;
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

    // Todo : 예외 클래스 변경 필요
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

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

    // Todo : 플레이리스트 소유자에게 알림 전송 이벤트 발행

  }

  @Transactional
  public void unsubscribePlaylist(String email, UUID playlistId) {

    // Todo : 예외 클래스 변경 필요
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

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
