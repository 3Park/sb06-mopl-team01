package org.example.mopl.contentevaluation.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.entity.ContentsStat;
import org.example.mopl.content.repository.ContentTagQueryRepository;
import org.example.mopl.content.repository.ContentsStatQueryRepository;
import org.example.mopl.contentevaluation.dto.ContentEvaluationQueryDto.CursorPlaylistPage;
import org.example.mopl.contentevaluation.dto.request.CursorRequestPlaylistDto;
import org.example.mopl.contentevaluation.dto.response.CursorResponsePlaylistDto;
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
import org.example.mopl.watchtogether.service.WatchTogetherService;
import org.springframework.data.domain.Page;
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
  private final WatchTogetherService watchTogetherService;

  // Todo : 쿼리 최적화 필요
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
        .findAllByContentIds(
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
                    watchTogetherService.getWatcherCount(String.valueOf(content.getId()))
                )
            )
            .toList()
    );

  }

  @Transactional(readOnly = true)
  public CursorResponsePlaylistDto getPlaylistListByCursor(CursorRequestPlaylistDto request) {

    Page<CursorPlaylistPage> playlistPage = playlistQueryRepository.findAllByCursor(request);

    Map<Long, List<PlaylistContent>> playlistContentsMap = playlistContentQueryRepository
        .findAllByPlaylistIds(
            playlistPage.getContent().stream()
                .map(CursorPlaylistPage::id)
                .toList()
        );

    Map<Long, List<String>> contentTagsMap = contentTagQueryRepository
        .findTagsByContentIds(
            playlistContentsMap.values().stream()
                .flatMap(List::stream)
                .map(content -> content.getContent().getId())
                .toList()
        );

    Map<Long, ContentsStat> contentsStatMap = contentsStatQueryRepository
        .findAllByContentIds(
            playlistContentsMap.values().stream()
                .flatMap(List::stream)
                .map(content -> content.getContent().getId())
                .toList()
        );

    List<PlaylistDto> playlistDtoList = playlistPage.getContent().stream()
        .map(playlist -> PlaylistDto.of(
            playlist.uuid(),
            OwnerDto.of(
                playlist.userUuid(),
                playlist.userName(),
                playlist.userProfileUrl()
            ),
            playlist.title(),
            playlist.description(),
            playlist.updatedAt(),
            playlist.subscriberCount(),
            subscribeQueryRepository.existsByUserIdAndPlaylistId(
                playlist.userId(),
                playlist.id()
            ),
            playlistContentsMap.get(playlist.id()).stream()
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
                        watchTogetherService.getWatcherCount(String.valueOf(content.getId()))
                    )
                )
                .toList()
        ))
        .toList();

    return CursorResponsePlaylistDto.builder()
        .data(playlistDtoList)
        .nextCursor(playlistPage.hasNext() ?
            playlistPage.getContent()
                .get(playlistPage.getContent().size() - 1).uuid().toString() : null)
        .hasNext(playlistPage.hasNext())
        .totalCount(playlistPage.getTotalElements())
        .sortBy(request.sortBy())
        .sortDirection(request.sortDirection())
        .build();



  }

}
