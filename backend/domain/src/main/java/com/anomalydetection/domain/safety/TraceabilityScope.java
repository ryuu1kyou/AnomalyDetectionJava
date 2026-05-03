package com.anomalydetection.domain.safety;

/** Publication scope for traceability records (automotive-safety skill). */
public enum TraceabilityScope {
  /** Applies to all OEMs — platform-level change. */
  PLATFORM,
  /** Applies to one specific OEM only. */
  OEM_SPECIFIC,
  /** Internal use only — not shared with OEMs. */
  INTERNAL_ONLY
}
