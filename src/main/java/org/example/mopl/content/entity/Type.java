package org.example.mopl.content.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum Type {

  MOVIE("movie"),TVSERIES("tvSeries"),SPORT("sport");

  private final String value;

  public static Type fromValue(String value) {
    for (Type type : Type.values()) {
      if (type.getValue().equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown Type value: " + value);
  }

}
