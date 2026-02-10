package org.example.mopl.content.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Entity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contents_watching_count")
public class ContentsWatchingCount {

  @Id
  @Tsid
  @Column(name = "id", nullable = false)
  protected Long id;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @Column(name = "watcher_count", nullable = false)
  private Long watcherCount = 0L;

  @LastModifiedDate
  @Column(name = "updated_at")
  protected Instant updatedAt;

  @Builder(access = AccessLevel.PROTECTED)
  public ContentsWatchingCount(Content content) {
    this.content = content;
  }

  public static ContentsWatchingCount of(Content content) {
    return ContentsWatchingCount.builder()
        .content(content)
        .build();
  }

  public void updateCount(Long newCount) {
    this.watcherCount = newCount;
  }

  public boolean isSameCount(Long count) {
    return this.watcherCount.equals(count);
  }

}
