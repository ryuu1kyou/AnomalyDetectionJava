package com.anomalydetection.domain.anomalydetection;

public enum AnomalyType {
  TIMEOUT(1),
  OUT_OF_RANGE(2),
  RATE_OF_CHANGE(3),
  STUCK(4),
  PERIODIC_ANOMALY(5),
  DATA_LOSS(6),
  NOISE(7),
  PATTERN_ANOMALY(8),
  CORRELATION_ANOMALY(9),
  CUSTOM(99);

  private final int value;

  AnomalyType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
