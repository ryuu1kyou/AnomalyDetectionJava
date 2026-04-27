package com.anomalydetection.domain.anomalydetection;

public enum AsilLevel {
  QM(0),
  A(1),
  B(2),
  C(3),
  D(4);

  private final int value;

  AsilLevel(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public boolean requiresApproval() {
    return this.value >= B.value;
  }
}
