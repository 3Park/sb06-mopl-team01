package org.example.mopl.contentevaluation.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.Content;
import org.example.mopl.content.exception.NoSuchAuthorException;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.example.mopl.contentevaluation.entity.Playlist;
import org.example.mopl.contentevaluation.entity.PlaylistContent;
import org.example.mopl.contentevaluation.exception.NoSuchPlaylistException;
import org.example.mopl.contentevaluation.exception.UnauthorizedPlaylistException;
import org.example.mopl.contentevaluation.repository.PlaylistContentCommandRepository;
import org.example.mopl.contentevaluation.repository.PlaylistContentQueryRepository;
import org.example.mopl.contentevaluation.repository.PlaylistQueryRepository;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaylistContentCommandService {

  private final ContentQueryRepository contentQueryRepository;
  private final PlaylistQueryRepository playlistQueryRepository;
  private final PlaylistContentCommandRepository playlistContentCommandRepository;
  private final PlaylistContentQueryRepository playlistContentQueryRepository;
  private final UserRepository userRepository;

  @Transactional
  public void addContentToPlaylist(String email, UUID playlistId, UUID contentId) {

    Content content = contentQueryRepository.findByUuid(contentId)
        .orElseThrow(() -> new NoSuchContentException(contentId));

    Playlist playlist = playlistQueryRepository.findByUuid(playlistId)
        .orElseThrow(() -> new NoSuchPlaylistException(playlistId));

    // Todo : 예외 클래스 변경 필요
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new NoSuchAuthorException(email));

    boolean isAdmin = user.getUserRoles().stream()
        .anyMatch(role -> role.getRole().getIsAdmin());

    if (!isAdmin && !playlist.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedPlaylistException(email, playlistId);
    }

    playlistContentCommandRepository.save(
      PlaylistContent.of(
        content,
        playlist
      )
    );

    // Todo : 구독 중인 사용자에게 알림 전송 이벤트 발행

  }

  @Transactional
  public void removeContentFromPlaylist(String email, UUID playlistId, UUID contentId) {

    Playlist playlist = playlistQueryRepository.findByUuid(playlistId)
        .orElseThrow(() -> new NoSuchPlaylistException(playlistId));

    // Todo : 예외 클래스 변경 필요
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new NoSuchAuthorException(email));

    boolean isAdmin = user.getUserRoles().stream()
        .anyMatch(role -> role.getRole().getIsAdmin());

    if (!isAdmin && !playlist.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedPlaylistException(email, playlistId);
    }

    Content content = contentQueryRepository.findByUuid(contentId)
        .orElseThrow(() -> new NoSuchContentException(contentId));

    // 삭제할 콘텐츠가 플레이리스트에 존재하는지 확인
    if (!playlistContentQueryRepository.existsByPlaylistIdAndContentId(playlist.getId(), content.getId())) {
      throw new NoSuchContentException("Content with ID " + contentId + " not found in playlist " + playlistId);
    }

    playlistContentCommandRepository.deleteByPlaylist_IdAndContent_Id(playlist.getId(), content.getId());

  }

}
