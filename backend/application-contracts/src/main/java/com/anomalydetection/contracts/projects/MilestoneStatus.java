package com.anomalydetection.contracts.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum MilestoneStatus {
  NotStarted(0),
  InProgress(1),
  Completed(2),
  Delayed(3),
  Cancelled(4);

  private final int code;

  MilestoneStatus(int code) {
    this.code = code;
  }

  @JsonValue
  public int code() {
    return code;
  }

  @JsonCreator
  public static MilestoneStatus fromCode(int code) {
    return Arrays.stream(values())
        .filter(v -> v.code == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown MilestoneStatus code: " + code));
  }
}
