package org.example.mopl.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.mopl.content.entity.basic.BasicContentEntity;
import org.springframework.data.annotation.CreatedDate;

@Getter
@Entity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "content_tags")
public class ContentTag extends BasicContentEntity {

  @JoinColumn(name = "content_id", nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Content content;

  @JoinColumn(name = "tag_id", nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Tag tag;

  @Builder(access = AccessLevel.PROTECTED)
  public ContentTag(Content content, Tag tag) {
    this.content = content;
    this.tag = tag;
  }

  public static ContentTag of(Content content, Tag tag) {
    return ContentTag.builder()
        .content(content)
        .tag(tag)
        .build();
  }



}
