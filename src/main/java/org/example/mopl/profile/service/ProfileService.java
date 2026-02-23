package org.example.mopl.profile.service;

import org.example.mopl.contentevaluation.dto.request.CursorRequestPlaylistDto;
import org.example.mopl.contentevaluation.dto.response.CursorResponsePlaylistDto;
import org.example.mopl.contentevaluation.service.PlaylistQueryService;
import org.example.mopl.profile.dto.SubscribedPlaylistCursorResponse;
import org.example.mopl.profile.dto.SubscribedPlaylistItemDto;
import org.example.mopl.profile.repository.SubscribedPlaylistQueryRepository;
import org.example.mopl.profile.dto.ProfileDto;
import org.example.mopl.profile.dto.ProfileUpdateRequest;
import org.example.mopl.profile.dto.WatchingContentDto;
import org.example.mopl.profile.entity.Profile;
import org.example.mopl.profile.entity.WatchingSession;
import org.example.mopl.profile.exception.ProfileForbiddenException;
import org.example.mopl.profile.exception.ProfileNotFoundException;
import org.example.mopl.profile.exception.ProfileUnauthorizedException;
import org.example.mopl.profile.repository.ProfileRepository;
import org.example.mopl.profile.repository.WatchingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileImageUploadService profileImageUploadService;
    private final WatchingSessionRepository watchingSessionRepository;
    private final PlaylistQueryService playlistQueryService;
    private final SubscribedPlaylistQueryRepository subscribedPlaylistQueryRepository;

    public ProfileService(ProfileRepository profileRepository, ProfileImageUploadService profileImageUploadService, WatchingSessionRepository watchingSessionRepository, PlaylistQueryService playlistQueryService, SubscribedPlaylistQueryRepository subscribedPlaylistQueryRepository) {
        this.profileRepository = profileRepository;
        this.profileImageUploadService = profileImageUploadService;
        this.watchingSessionRepository = watchingSessionRepository;
        this.playlistQueryService = playlistQueryService;
        this.subscribedPlaylistQueryRepository = subscribedPlaylistQueryRepository;
    }

    @Transactional(readOnly = true)
    public ProfileDto getByUserUuid(UUID userUuid) {
        Profile profile = profileRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new ProfileNotFoundException(userUuid));
        return toDto(profile);
    }

    @Transactional
    public ProfileDto update(UUID userUuid, ProfileUpdateRequest request, UUID currentUserUuid) {
        if (currentUserUuid == null) {
            throw new ProfileUnauthorizedException();
        }
        Profile profile = profileRepository.findWithUserByUserUuid(userUuid)
                .orElseThrow(() -> new ProfileNotFoundException(userUuid));
        if (!profile.getUser().getUuid().equals(currentUserUuid)) {
            throw new ProfileForbiddenException();
        }
        profile.update(request.getName(), request.getProfileImageUrl());
        return toDto(profile);
    }

    @Transactional
    public ProfileDto uploadProfileImage(UUID userUuid, MultipartFile file, UUID currentUserUuid) throws IOException {
        if (currentUserUuid == null) {
            throw new ProfileUnauthorizedException();
        }
        Profile profile = profileRepository.findWithUserByUserUuid(userUuid)
                .orElseThrow(() -> new ProfileNotFoundException(userUuid));
        if (!profile.getUser().getUuid().equals(currentUserUuid)) {
            throw new ProfileForbiddenException();
        }
        String imageUrl = profileImageUploadService.upload(profile, file);
        profile.updateProfileImageUrl(imageUrl);
        return toDto(profile);
    }

    @Transactional(readOnly = true)
    public List<WatchingContentDto> getWatchingContents(UUID userUuid) {
        Profile profile = profileRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new ProfileNotFoundException(userUuid));
        List<WatchingSession> sessions = watchingSessionRepository.findActiveByWatcherId(profile.getUser().getId());
        return sessions.stream()
                .map(this::toWatchingContentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CursorResponsePlaylistDto getOwnedPlaylists(UUID userUuid, CursorRequestPlaylistDto request) {
        Profile profile = profileRepository.findWithUserByUserUuid(userUuid)
                .orElseThrow(() -> new ProfileNotFoundException(userUuid));
        UUID ownerUuid = profile.getUser().getUuid();
        CursorRequestPlaylistDto requestWithOwner = new CursorRequestPlaylistDto(
                request.keywordLike(),
                ownerUuid,
                request.subscriberIdEqual(),
                request.cursor(),
                request.idAfter(),
                request.limit(),
                request.sortDirection(),
                request.sortBy()
        );
        return playlistQueryService.getPlaylistListByCursor(null, requestWithOwner);
    }

    @Transactional(readOnly = true)
    public SubscribedPlaylistCursorResponse getSubscribedPlaylists(UUID userUuid, CursorRequestPlaylistDto request) {
        Profile profile = profileRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new ProfileNotFoundException(userUuid));
        Long userId = profile.getUser().getId();
        Instant cursor = request.cursor() != null && !request.cursor().isBlank()
                ? Instant.parse(request.cursor())
                : null;
        var result = subscribedPlaylistQueryRepository.findSubscribedByUserId(
                userId,
                request.limit(),
                request.sortBy(),
                request.sortDirection(),
                cursor,
                request.idAfter()
        );
        List<SubscribedPlaylistItemDto> items = result.data().stream()
                .map(row -> SubscribedPlaylistItemDto.builder()
                        .playlistUuid(row.playlistUuid())
                        .title(row.title())
                        .description(row.description())
                        .ownerUuid(row.ownerUuid())
                        .ownerName(row.ownerName())
                        .ownerProfileImageUrl(row.ownerProfileImageUrl())
                        .subscriberCount(row.subscriberCount() != null ? row.subscriberCount() : 0L)
                        .updatedAt(row.updatedAt())
                        .build())
                .collect(Collectors.toList());
        return SubscribedPlaylistCursorResponse.builder()
                .data(items)
                .nextCursor(result.nextCursor())
                .nextIdAfter(result.nextIdAfter())
                .hasNext(result.hasNext())
                .sortBy(request.sortBy())
                .sortDirection(request.sortDirection())
                .build();
    }

    private WatchingContentDto toWatchingContentDto(WatchingSession session) {
        var c = session.getContent();
        return WatchingContentDto.builder()
                .sessionUuid(session.getUuid())
                .contentId(c.getId())
                .contentUuid(c.getUuid())
                .contentTitle(c.getTitle())
                .contentDescription(c.getDescription())
                .contentThumbnailUrl(c.getThumbnailUrl())
                .contentType(c.getContentType() != null ? c.getContentType().getValue() : null)
                .lastPosition(session.getLastPosition())
                .duration(session.getDuration())
                .status(session.getStatus().name())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private ProfileDto toDto(Profile profile) {
        return ProfileDto.builder()
                .id(profile.getId())
                .uuid(profile.getUuid())
                .userUuid(profile.getUser().getUuid())
                .name(profile.getName())
                .profileImageUrl(profile.getProfileImageUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}


