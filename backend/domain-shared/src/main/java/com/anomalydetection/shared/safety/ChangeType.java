package com.anomalydetection.shared.safety;

/** How a change propagates across module/interface boundaries (automotive-safety skill). */
public enum ChangeType {
  /** Normal day-to-day code change within a single module — no interface boundary crossed. */
  DAILY_CHANGE,
  /** Change crosses an interface boundary and may impact multiple modules (IF-impact event). */
  INTEGRATION_EVENT
}
