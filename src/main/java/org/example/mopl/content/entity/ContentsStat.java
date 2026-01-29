package org.example.mopl.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Entity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contents_stats")
public class ContentsStat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @JoinColumn(name = "content_id", nullable = false)
  @OneToOne(optional = false, fetch = FetchType.LAZY)
  private Content content;

  @Column(name = "rating_count", nullable = false)
  private Long ratingCount = 0L;

  @Column(name = "rating_sum", nullable = false)
  private Long ratingSum = 0L;

  @Column(name = "rating_average", nullable = false)
  private Double ratingAverage = 0.0;

  @Builder(access = AccessLevel.PROTECTED)
  public ContentsStat(Content content) {
    this.content = content;
  }

  public static ContentsStat of(Content content) {
    return ContentsStat.builder()
        .content(content)
        .build();
  }

  public void addRating(long rating) {
    this.ratingCount += 1;
    this.ratingSum += rating;
    this.ratingAverage = this.ratingSum.doubleValue() / this.ratingCount.doubleValue();
  }

}
