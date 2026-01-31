package org.example.mopl.content.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.ContentQueryDto.ReviewResult;
import org.example.mopl.content.dto.request.CursorRequestReviewDto;
import org.example.mopl.content.dto.response.AuthorDto;
import org.example.mopl.content.dto.response.CursorResponseReviewDto;
import org.example.mopl.content.dto.response.ReviewDto;
import org.example.mopl.content.repository.ReviewQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {

  private final ReviewQueryRepository reviewQueryRepository;

  public CursorResponseReviewDto getReviewsByCursor(CursorRequestReviewDto request) {

    Page<ReviewResult> reviewPage = reviewQueryRepository.findAllByCursor(request);

    List<ReviewDto> reviewDtoList = reviewPage.getContent().stream()
        .map(review ->
          ReviewDto.of(
              review.uuid(),
              review.contentId(),
              AuthorDto.of(
                  review.userId(),
                  review.userName(),
                  review.userProfileUrl()
              ),
              review.text(),
              review.rating()
          )
        )
        .toList();

    return CursorResponseReviewDto.builder()
        .data(reviewDtoList)
        .nextCursor(reviewPage.hasNext() ? reviewPage.getContent().get(reviewPage.getSize() - 1).createdAt().toString() : null)
        .nextIdAfter(reviewPage.hasNext() ? reviewPage.getContent().get(reviewPage.getSize() - 1).uuid() : null)
        .hasNext(reviewPage.hasNext())
        .totalCount(reviewPage.getTotalElements())
        .sortBy(request.sortBy())
        .sortDirection(request.sortDirection())
        .build();

  }



}
