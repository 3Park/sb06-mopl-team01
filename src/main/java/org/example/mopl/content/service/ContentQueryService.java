package org.example.mopl.content.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.request.CursorRequestContentDto;
import org.example.mopl.content.dto.response.ContentDto;
import org.example.mopl.content.dto.response.CursorResponseContentDto;
import org.example.mopl.content.exception.NoSuchContentException;
import org.example.mopl.content.repository.ContentQueryRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentQueryService {

  private final ContentQueryRepository contentQueryRepository;

  // Todo : watcherCount를 실시간 같이보기 모듈에서 가져오기로 대체
  public ContentDto getContentByUuid(UUID uuid) {
    return contentQueryRepository.findByUuidWithContentTag(uuid)
        .orElseThrow(() -> new NoSuchContentException("존재하지 않는 콘텐츠입니다. UUID: " + uuid));
  }

  // Todo : 커서 기반 페이지네이션 (watcherCount로 정렬해야 하므로 실시간 같이보기 모듈 필요)
  public CursorResponseContentDto getContentsByCursor(CursorRequestContentDto request) {
    return null;
  }

}
