package com.anomalydetection.shared.safety;

/** Document synchronisation status for the 3-ledger model (automotive-safety skill). */
public enum DocSyncStatus {
  /** Document update is not required for this change. */
  NOT_REQUIRED,
  /** Document update is required but not yet done. */
  PENDING,
  /** Document has been updated. */
  UPDATED,
  /** Document has been reviewed and confirmed. */
  REVIEWED
}
