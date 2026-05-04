package com.anomalydetection.contracts.safety;

import com.anomalydetection.shared.safety.LifecycleStage;

public record VerificationRecordDto(
    String id,
    LifecycleStage stage,
    String method,
    String result,
    String verifiedBy,
    String verifiedAt,
    String toolRef,
    String notes) {}
