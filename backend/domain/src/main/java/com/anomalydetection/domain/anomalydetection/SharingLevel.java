package com.anomalydetection.domain.anomalydetection;

public enum SharingLevel {
  PRIVATE(0),
  OEM_PARTNER(1),
  INDUSTRY(2),
  PUBLIC(3);

  private final int value;

  SharingLevel(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
