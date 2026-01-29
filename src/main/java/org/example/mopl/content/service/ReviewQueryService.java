package org.example.mopl.content.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.dto.request.CursorRequestReviewDto;
import org.example.mopl.content.dto.response.AuthorDto;
import org.example.mopl.content.dto.response.CursorResponseReviewDto;
import org.example.mopl.content.dto.response.ReviewDto;
import org.example.mopl.content.entity.Review;
import org.example.mopl.content.repository.ReviewQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {

  private final ReviewQueryRepository reviewQueryRepository;

  public CursorResponseReviewDto getReviewsByCursor(CursorRequestReviewDto request) {

    Page<Review> reviewPage = reviewQueryRepository.findAllByCursor(request);

    List<ReviewDto> reviewDtoList = reviewPage.stream()
        .map(review ->
          ReviewDto.of(
              review.getUuid(),
              review.getContent().getUuid(),
              AuthorDto.of(
                  review.getUser().getUuid(),
                  review.getUser().getProfile().getName(),
                  review.getUser().getProfile().getProfileImageUrl()
              ),
              review.getText(),
              review.getRating()
          )
        )
        .toList();

    return CursorResponseReviewDto.builder()
        .data(reviewDtoList)
        .nextCursor(reviewPage.hasNext() ? reviewPage.getContent().get(reviewPage.getSize() - 1).getCreatedAt().toString() : null)
        .nextIdAfter(reviewPage.hasNext() ? reviewPage.getContent().get(reviewPage.getSize() - 1).getUuid() : null)
        .hasNext(reviewPage.hasNext())
        .totalCount(reviewPage.getTotalElements())
        .sortBy(request.sortBy())
        .sortDirection(request.sortDirection())
        .build();

  }



}
