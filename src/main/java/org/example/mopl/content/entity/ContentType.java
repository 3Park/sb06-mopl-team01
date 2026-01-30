package org.example.mopl.content.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ContentType {

  MOVIE("movie"),TVSERIES("tvSeries"),SPORT("sport");

  private final String value;

  public static ContentType fromValue(String value) {
    for (ContentType contentType : ContentType.values()) {
      if (contentType.getValue().equalsIgnoreCase(value)) {
        return contentType;
      }
    }
    throw new IllegalArgumentException("Unknown Type value: " + value);
  }

}
