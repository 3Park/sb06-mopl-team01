package org.example.mopl.contentevaluation.service;

import java.util.ArrayList;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.contentevaluation.dto.request.PlaylistCreateRequest;
import org.example.mopl.contentevaluation.dto.request.PlaylistUpdateRequest;
import org.example.mopl.contentevaluation.dto.response.OwnerDto;
import org.example.mopl.contentevaluation.dto.response.PlaylistDto;
import org.example.mopl.contentevaluation.entity.Playlist;
import org.example.mopl.contentevaluation.entity.PlaylistsStat;
import org.example.mopl.contentevaluation.entity.QSubscribe;
import org.example.mopl.contentevaluation.entity.Subscribe;
import org.example.mopl.contentevaluation.exception.NoSuchPlaylistException;
import org.example.mopl.contentevaluation.exception.UnauthorizedPlaylistException;
import org.example.mopl.contentevaluation.repository.PlaylistCommandRepository;
import org.example.mopl.contentevaluation.repository.PlaylistContentCommandRepository;
import org.example.mopl.contentevaluation.repository.PlaylistQueryRepository;
import org.example.mopl.contentevaluation.repository.PlaylistStatCommandRepository;
import org.example.mopl.contentevaluation.repository.PlaylistStatQueryRepository;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaylistCommandService {

  private final PlaylistCommandRepository playlistCommandRepository;
  private final PlaylistQueryRepository playlistQueryRepository;
  private final PlaylistContentCommandRepository playlistContentCommandRepository;
  private final PlaylistStatCommandRepository playlistStatCommandRepository;
  private final PlaylistStatQueryRepository playlistStatQueryRepository;
  private final UserRepository userRepository;

  @Transactional
  public PlaylistDto createPlaylist(String email, PlaylistCreateRequest request) {

    // Todo : 예외 클래스 변경 필요
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

    Playlist playlist = Playlist.of(request.title(), user, request.description());

    Playlist savedPlaylist = playlistCommandRepository.save(playlist);

    return PlaylistDto.of(
        playlist.getUuid(),
        OwnerDto.of(
            user.getUuid(),
            user.getProfile().getName(),
            user.getProfile().getProfileImageUrl()
        ),
        savedPlaylist.getTitle(),
        savedPlaylist.getDescription(),
        savedPlaylist.getUpdatedAt(),
        0L,
        true,
        new ArrayList<>()
    );

  }

  @Transactional
  public PlaylistDto updatePlaylist(String email, UUID playlistId, PlaylistUpdateRequest request) {

    Playlist playlist = playlistQueryRepository.findByUuid(playlistId)
        .orElseThrow(() -> new NoSuchPlaylistException(playlistId));

    // Todo : 예외 클래스 변경 필요
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

    boolean isAdmin = user.getUserRoles().stream()
        .anyMatch(role -> role.getRole().getIsAdmin());

    if (!isAdmin && !playlist.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedPlaylistException(email, playlistId);
    }

    PlaylistsStat playlistsStat = playlistStatQueryRepository.findByPlaylistId(playlist.getId())
        .orElseThrow(() -> new NoSuchPlaylistException(playlistId));

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
        .orElseThrow(() -> new NoSuchPlaylistException(playlistId));

    // Todo : 예외 클래스 변경 필요
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

    boolean isAdmin = user.getUserRoles().stream()
        .anyMatch(role -> role.getRole().getIsAdmin());

    if (!isAdmin && !playlist.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedPlaylistException(email, playlistId);
    }

    playlistContentCommandRepository.deleteByPlaylist_Id(playlist.getId());
    playlistStatCommandRepository.deleteByPlaylist_Id(playlist.getId());
    playlistCommandRepository.deleteById(playlist.getId());

  }


}
