package org.example.mopl.contentevaluation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.Content;
import org.springframework.data.annotation.CreatedDate;

@Getter
@Entity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "playlist_contents")
public class PlaylistContents {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @JoinColumn(name = "content_id", nullable = false)
  @ManyToOne(optional = false)
  private Content content;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

}
