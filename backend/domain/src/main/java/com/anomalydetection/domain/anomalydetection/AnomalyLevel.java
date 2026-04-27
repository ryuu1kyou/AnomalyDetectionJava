package com.anomalydetection.domain.anomalydetection;

public enum AnomalyLevel {
  INFO(0),
  WARNING(1),
  ERROR(2),
  CRITICAL(3),
  FATAL(4);

  private final int value;

  AnomalyLevel(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
