package com.anomalydetection.contracts.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum ProjectStatus {
  Planning(0),
  Active(1),
  OnHold(2),
  Completed(3),
  Cancelled(4);

  private final int code;

  ProjectStatus(int code) {
    this.code = code;
  }

  @JsonValue
  public int code() {
    return code;
  }

  @JsonCreator
  public static ProjectStatus fromCode(int code) {
    return Arrays.stream(values())
        .filter(v -> v.code == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown ProjectStatus code: " + code));
  }
}
