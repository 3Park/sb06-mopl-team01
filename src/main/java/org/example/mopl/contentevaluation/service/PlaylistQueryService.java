package org.example.mopl.contentevaluation.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.entity.ContentsStat;
import org.example.mopl.content.repository.ContentTagQueryRepository;
import org.example.mopl.content.repository.ContentsStatQueryRepository;
import org.example.mopl.contentevaluation.dto.response.OwnerDto;
import org.example.mopl.contentevaluation.dto.response.PlaylistDto;
import org.example.mopl.contentevaluation.entity.Playlist;
import org.example.mopl.contentevaluation.entity.PlaylistContent;
import org.example.mopl.contentevaluation.entity.PlaylistsStat;
import org.example.mopl.contentevaluation.exception.NoSuchPlaylistException;
import org.example.mopl.contentevaluation.repository.PlaylistContentQueryRepository;
import org.example.mopl.contentevaluation.repository.PlaylistQueryRepository;
import org.example.mopl.contentevaluation.repository.PlaylistsStatQueryRepository;
import org.example.mopl.contentevaluation.repository.SubscribeQueryRepository;
import org.example.mopl.watchtogether.service.BasicWatchTogetherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaylistQueryService {

  private final PlaylistQueryRepository playlistQueryRepository;
  private final PlaylistContentQueryRepository playlistContentQueryRepository;
  private final PlaylistsStatQueryRepository playlistsStatQueryRepository;
  private final ContentTagQueryRepository contentTagQueryRepository;
  private final ContentsStatQueryRepository contentsStatQueryRepository;
  private final SubscribeQueryRepository subscribeQueryRepository;
  private final BasicWatchTogetherService basicWatchTogetherService;

  // 플레이리스트 단건 조회
  @Transactional(readOnly = true)
  public PlaylistDto getPlaylistDtoByUuid(UUID uuid) {

    Playlist playlist = playlistQueryRepository.findByUuid(uuid)
        .orElseThrow(() -> new NoSuchPlaylistException(uuid));

    List<PlaylistContent> playlistContents = playlistContentQueryRepository
        .findAllByPlaylistId(playlist.getId());

    PlaylistsStat playlistsStat = playlistsStatQueryRepository.findByPlaylistId(playlist.getId())
        .orElseThrow(() -> new NoSuchPlaylistException(uuid));

    Map<Long, List<String>> contentTagsMap = contentTagQueryRepository
        .findTagsByContentIds(
            playlistContents.stream()
                .map(content -> content.getContent().getId())
                .toList()
        );

    Map<Long, ContentsStat> contentsStatMap = contentsStatQueryRepository
        .getContentsStatByContentIds(
            playlistContents.stream()
                .map(content -> content.getContent().getId())
                .toList()
        );

    return PlaylistDto.of(
        playlist.getUuid(),
        OwnerDto.of(
            playlist.getUser().getUuid(),
            playlist.getUser().getProfile().getName(),
            playlist.getUser().getProfile().getProfileImageUrl()
        ),
        playlist.getTitle(),
        playlist.getDescription(),
        playlist.getUpdatedAt(),
        playlistsStat.getSubscribeCount(),
        subscribeQueryRepository.existsByUserIdAndPlaylistId(
            playlist.getUser().getId(),
            playlist.getId()
        ),
        playlistContents.stream()
            .map(content ->
                ContentDto.of(
                    content.getContent().getUuid(),
                    content.getContent().getContentType().getValue(),
                    content.getContent().getTitle(),
                    content.getContent().getDescription(),
                    content.getContent().getThumbnailUrl(),
                    contentTagsMap.get(content.getId()),
                    contentsStatMap.get(content.getId()).getRatingAverage(),
                    contentsStatMap.get(content.getId()).getRatingCount(),
                    basicWatchTogetherService.getWatcherCount(String.valueOf(content.getId()))
                )
            )
            .toList()
    );

  }

}
