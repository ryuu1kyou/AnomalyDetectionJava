package com.anomalydetection.shared.safety;

/** ISO 26262 V-model lifecycle stage for a traceability record. */
public enum LifecycleStage {
  REQUIREMENTS_DEFINITION,
  SYSTEM_DESIGN,
  SOFTWARE_DESIGN,
  IMPLEMENTATION,
  UNIT_TESTING,
  INTEGRATION_TESTING,
  VERIFICATION,
  VALIDATION,
  RELEASE,
  OPERATION
}
