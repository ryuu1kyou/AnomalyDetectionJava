package com.anomalydetection.contracts.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum ProjectPriority {
  Low(1),
  Medium(2),
  High(3),
  Critical(4);

  private final int code;

  ProjectPriority(int code) {
    this.code = code;
  }

  @JsonValue
  public int code() {
    return code;
  }

  @JsonCreator
  public static ProjectPriority fromCode(int code) {
    return Arrays.stream(values())
        .filter(v -> v.code == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown ProjectPriority code: " + code));
  }
}
