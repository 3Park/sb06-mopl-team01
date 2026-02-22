package org.example.mopl.contentevaluation.service;

import java.util.ArrayList;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.content.exception.ContentErrorCode;
import org.example.mopl.content.exception.ContentException;
import org.example.mopl.contentevaluation.dto.request.PlaylistCreateRequest;
import org.example.mopl.contentevaluation.dto.request.PlaylistUpdateRequest;
import org.example.mopl.contentevaluation.dto.response.OwnerDto;
import org.example.mopl.contentevaluation.dto.response.PlaylistDto;
import org.example.mopl.contentevaluation.entity.Playlist;
import org.example.mopl.contentevaluation.entity.PlaylistsStat;
import org.example.mopl.contentevaluation.event.CreatePlaylistEvent;
import org.example.mopl.contentevaluation.exception.ContentEvaluationErrorCode;
import org.example.mopl.contentevaluation.exception.ContentEvaluationException;
import org.example.mopl.contentevaluation.repository.PlaylistCommandRepository;
import org.example.mopl.contentevaluation.repository.PlaylistContentCommandRepository;
import org.example.mopl.contentevaluation.repository.PlaylistQueryRepository;
import org.example.mopl.contentevaluation.repository.PlaylistsStatCommandRepository;
import org.example.mopl.contentevaluation.repository.PlaylistsStatQueryRepository;
import org.example.mopl.event.message.PlaylistCreatedKafkaEvent;
import org.example.mopl.profile.repository.FollowRepository;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistCommandService {

  private final PlaylistCommandRepository playlistCommandRepository;
  private final PlaylistQueryRepository playlistQueryRepository;
  private final PlaylistContentCommandRepository playlistContentCommandRepository;
  private final PlaylistsStatCommandRepository playlistsStatCommandRepository;
  private final PlaylistsStatQueryRepository playlistsStatQueryRepository;
  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public PlaylistDto createPlaylist(String email, PlaylistCreateRequest request) {

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_AUTHOR));

    Playlist playlist = Playlist.of(request.title(), user, request.description());

    Playlist savedPlaylist = playlistCommandRepository.save(playlist);

    // 통계성 엔티티 생성 이벤트 발행
    eventPublisher.publishEvent(
        CreatePlaylistEvent.of(savedPlaylist)
    );

    // 팔로우 중인 사용자에게 플레이리스트 생성 알림
    eventPublisher.publishEvent(
        PlaylistCreatedKafkaEvent.of(
          followRepository.findAllByFolloweeIdWithFollower(
              user.getId()
          ).stream().map(follow -> follow.getFollower().getUuid()).toList(),
          user.getProfile().getName(),
          savedPlaylist.getTitle(),
          savedPlaylist.getDescription()
      )
    );

    return PlaylistDto.of(
        savedPlaylist.getUuid(),
        OwnerDto.of(
            user.getUuid(),
            user.getProfile().getName(),
            user.getProfile().getProfileImageUrl()
        ),
        savedPlaylist.getTitle(),
        savedPlaylist.getDescription(),
        savedPlaylist.getUpdatedAt(),
        0L,
        false,
        new ArrayList<>()
    );

  }

  @Transactional
  public PlaylistDto updatePlaylist(String email, UUID playlistId, PlaylistUpdateRequest request) {

    Playlist playlist = playlistQueryRepository.findByUuid(playlistId)
        .orElseThrow(() -> new ContentEvaluationException(ContentEvaluationErrorCode.NO_SUCH_PLAYLIST));

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_AUTHOR));

    boolean isAdmin = user.getUserRoles().stream()
        .anyMatch(role -> role.getRole().getIsAdmin());

    // 작성자 본인이나 관리자가 아닌 경우 예외 발생
    if (!isAdmin && !playlist.getUser().getId().equals(user.getId())) {
      throw new ContentEvaluationException(ContentEvaluationErrorCode.UNAUTHORIZED_PLAYLIST);
    }

    PlaylistsStat playlistsStat = playlistsStatQueryRepository.findByPlaylistId(playlist.getId())
        .orElseThrow(() -> new ContentEvaluationException(ContentEvaluationErrorCode.NO_SUCH_PLAYLIST));

    playlist.update(request.title(), request.description());

    return PlaylistDto.of(
        playlist.getUuid(),
        OwnerDto.of(
            user.getUuid(),
            user.getProfile().getName(),
            user.getProfile().getProfileImageUrl()
        ),
        playlist.getTitle(),
        playlist.getDescription(),
        playlist.getUpdatedAt(),
        playlistsStat.getSubscribeCount(),
        true,
        new ArrayList<>()
    );

  }

  @Transactional
  public void deletePlaylistByUuid(String email, UUID playlistId) {

    Playlist playlist = playlistQueryRepository.findByUuid(playlistId)
        .orElseThrow(() -> new ContentEvaluationException(ContentEvaluationErrorCode.NO_SUCH_PLAYLIST));

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ContentException(ContentErrorCode.NO_SUCH_AUTHOR));

    boolean isAdmin = user.getUserRoles().stream()
        .anyMatch(role -> role.getRole().getIsAdmin());

    // 작성자 본인이나 관리자가 아닌 경우 예외 발생
    if (!isAdmin && !playlist.getUser().getId().equals(user.getId())) {
      throw new ContentEvaluationException(ContentEvaluationErrorCode.UNAUTHORIZED_PLAYLIST);
    }

    // 연관관계 삭제
    playlistContentCommandRepository.deleteByPlaylist_Id(playlist.getId());
    playlistsStatCommandRepository.deleteByPlaylist_Id(playlist.getId());

    playlistCommandRepository.deleteById(playlist.getId());

  }


}
