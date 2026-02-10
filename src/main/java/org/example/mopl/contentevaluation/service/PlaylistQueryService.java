package org.example.mopl.contentevaluation.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.entity.ContentsStat;
import org.example.mopl.content.repository.ContentTagQueryRepository;
import org.example.mopl.content.repository.ContentsStatQueryRepository;
import org.example.mopl.content.s3.ContentS3Client;
import org.example.mopl.contentevaluation.dto.ContentEvaluationQueryDto.PlaylistResult;
import org.example.mopl.contentevaluation.dto.request.CursorRequestPlaylistDto;
import org.example.mopl.contentevaluation.dto.response.CursorResponsePlaylistDto;
import org.example.mopl.contentevaluation.dto.response.OwnerDto;
import org.example.mopl.contentevaluation.dto.response.PlaylistDto;
import org.example.mopl.contentevaluation.entity.PlaylistContent;
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
  private final ContentS3Client contentS3Client;

  // 플레이리스트 단건 조회
  @Transactional(readOnly = true)
  public PlaylistDto getPlaylistByUuid(UUID uuid) {

    PlaylistResult playlist = playlistQueryRepository.findByUuidWithStats(uuid)
        .orElseThrow(() -> new NoSuchPlaylistException(uuid));

    List<PlaylistContent> playlistContents = playlistContentQueryRepository
        .findAllByPlaylistId(playlist.id());

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
        playlistContents.stream()
            .map(content ->
                ContentDto.of(
                    content.getContent().getUuid(),
                    content.getContent().getContentType().getValue(),
                    content.getContent().getTitle(),
                    content.getContent().getDescription(),
                    contentS3Client.getPresignedUrl(content.getContent().getThumbnailUrl()),
                    contentTagsMap.getOrDefault(content.getContent().getId(), List.of()),
                    contentsStatMap.containsKey(content.getContent().getId()) ?
                        contentsStatMap.get(content.getContent().getId()).getRatingAverage() : 0.0,
                    contentsStatMap.containsKey(content.getContent().getId()) ?
                        contentsStatMap.get(content.getContent().getId()).getRatingCount() : 0,
                    watchTogetherService.getWatcherCount(String.valueOf(content.getId()))
                )
            )
            .toList()
    );

  }

  // 플레이리스트 커서 기반 페이징
  @Transactional(readOnly = true)
  public CursorResponsePlaylistDto getPlaylistListByCursor(CursorRequestPlaylistDto request) {

    Page<PlaylistResult> playlistPage = playlistQueryRepository.findAllByCursor(request);

    Map<Long, List<PlaylistContent>> playlistContentsMap = playlistContentQueryRepository
        .findAllMapByPlaylistIds(
            playlistPage.getContent().stream()
                .map(PlaylistResult::id)
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
            playlistContentsMap.containsKey(playlist.id()) ?
            playlistContentsMap.get(playlist.id()).stream()
                .map(content ->
                    ContentDto.of(
                        content.getContent().getUuid(),
                        content.getContent().getContentType().getValue(),
                        content.getContent().getTitle(),
                        content.getContent().getDescription(),
                        contentS3Client.getPresignedUrl(content.getContent().getThumbnailUrl()),
                        contentTagsMap.getOrDefault(content.getContent().getId(), List.of()),
                        contentsStatMap.containsKey(content.getContent().getId()) ?
                            contentsStatMap.get(content.getContent().getId()).getRatingAverage() : 0.0,
                        contentsStatMap.containsKey(content.getContent().getId()) ?
                            contentsStatMap.get(content.getContent().getId()).getRatingCount() : 0,
                        watchTogetherService.getWatcherCount(String.valueOf(content.getId()))
                    )
                )
                .toList() : List.of()
        ))
        .toList();

    return CursorResponsePlaylistDto.builder()
        .data(playlistDtoList)
        .nextCursor(playlistPage.hasNext() ?
            switch (request.sortBy()) {
              case "subscribeCount" ->
                  playlistPage.getContent().get(playlistPage.getNumberOfElements() - 1).subscriberCount().toString();
              default ->
                  String.valueOf(playlistPage.getContent().get(playlistPage.getNumberOfElements() - 1).updatedAt().toString());
            }
             : null)
        .hasNext(playlistPage.hasNext())
        .totalCount(playlistPage.getTotalElements())
        .sortBy(request.sortBy())
        .sortDirection(request.sortDirection())
        .build();



  }

}
