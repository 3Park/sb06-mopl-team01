package org.example.mopl.contentevaluation.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.contentevaluation.repository.PlaylistCommandRepository;
import org.example.mopl.contentevaluation.repository.PlaylistQueryRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistCommandService {

  private final PlaylistCommandRepository playlistCommandRepository;
  private final PlaylistQueryRepository playlistQueryRepository;

}
