package org.example.mopl.contentevaluation.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.example.mopl.MoplApplication;
import org.example.mopl.common.config.QueryDslConfig;
import org.example.mopl.contentevaluation.dto.ContentEvaluationQueryDto.PlaylistResult;
import org.example.mopl.contentevaluation.dto.request.CursorRequestPlaylistDto;
import org.example.mopl.contentevaluation.entity.Playlist;
import org.example.mopl.contentevaluation.entity.PlaylistsStat;
import org.example.mopl.profile.entity.Profile;
import org.example.mopl.profile.repository.ProfileRepository;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, MoplApplication.class, PlaylistQueryRepository.class})
class PlaylistQueryRepositoryTest {

  @Autowired
  private PlaylistQueryRepository playlistQueryRepository;

  @Autowired
  private PlaylistCommandRepository playlistCommandRepository;

  @Autowired
  private PlaylistsStatCommandRepository playlistsStatCommandRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ProfileRepository profileRepository;

  private List<Playlist> playlists = new ArrayList<>();

  @BeforeEach
  void setUp() {

    User user = new User("testuser", "1234");
    userRepository.save(user);

    Profile profile = Profile.builder()
        //.uuid(UUID.randomUUID())
        .profileImageUrl("profileImageUrl")
        .name("Test User")
        .user(user)
        .build();
    profileRepository.save(profile);

    user.setProfile(profile);

    for (int i = 1; i <= 30; i++) {
      Playlist playlist = Playlist.of(
          "Playlist " + i,
          user,
          "Description for playlist " + i
      );
      playlists.add(playlist);
    }

    playlists = playlistCommandRepository.saveAll(playlists);

    playlistsStatCommandRepository.saveAll(
        playlists.stream()
            .map(PlaylistsStat::of)
            .toList()
    );

  }

  @AfterEach
  void tearDown() {
    playlistsStatCommandRepository.deleteAll();
    playlistCommandRepository.deleteAll();
    profileRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void findAllByCursor() {

    // given
    CursorRequestPlaylistDto request = new CursorRequestPlaylistDto(
        null,
        null,
        null,
        null,
        null,
        10,
        "DESC",
        "updatedAt"
    );

    // when
    Page<PlaylistResult> result = playlistQueryRepository.findAllByCursor(null, request);

    // then
    assertEquals(10, result.getContent().size());

  }
}