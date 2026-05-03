package com.anomalydetection.shared.safety;

/** Interface impact classification — TOP2 field (automotive-safety skill). */
public enum IfImpact {
  /** The interface is changed by this record/decision. */
  CHANGED,
  /** The interface is confirmed unchanged. */
  UNCHANGED,
  /** Impact is not yet determined — requires unknown_until + unknown_owner_id. */
  UNKNOWN
}
