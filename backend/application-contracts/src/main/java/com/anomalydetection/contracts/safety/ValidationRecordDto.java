package com.anomalydetection.contracts.safety;

import com.anomalydetection.shared.safety.LifecycleStage;

public record ValidationRecordDto(
    String id,
    LifecycleStage stage,
    String scenario,
    String outcome,
    String validatedBy,
    String validatedAt,
    String testRef,
    String notes) {}
