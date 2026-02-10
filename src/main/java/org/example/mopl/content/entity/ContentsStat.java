package org.example.mopl.content.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.exception.InvalidRatingDecreaseException;

@Getter
@Entity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contents_stats")
public class ContentsStat {

  @Id
  @Tsid
  @Column(name = "id", nullable = false)
  private Long id;

  @JoinColumn(name = "contents_id", nullable = false)
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

  public void removeRating(long rating) {

    if (this.ratingCount <= 0) {
      throw new InvalidRatingDecreaseException(content.getUuid());
    }

    this.ratingCount -= 1;
    this.ratingSum -= rating;
    if (this.ratingCount == 0) {
      this.ratingAverage = 0.0;
    } else {
      this.ratingAverage = this.ratingSum.doubleValue() / this.ratingCount.doubleValue();
    }

  }

}
