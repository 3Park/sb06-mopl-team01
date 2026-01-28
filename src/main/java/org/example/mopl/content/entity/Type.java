package org.example.mopl.content.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum Type {

  MOVIE("movie"),TVSERIES("tvSeries"),SPORT("sport");

  private final String value;

}
