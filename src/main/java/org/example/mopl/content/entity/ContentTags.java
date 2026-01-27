package org.example.mopl.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

@Getter
@Entity
@Table(name = "content_tags")
public class ContentTags {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @JoinColumn(name = "content_id", nullable = false)
  @ManyToOne(optional = false)
  private Content content;

  @JoinColumn(name = "tag_id", nullable = false)
  @ManyToOne(optional = false)
  private Tags tag;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

}
