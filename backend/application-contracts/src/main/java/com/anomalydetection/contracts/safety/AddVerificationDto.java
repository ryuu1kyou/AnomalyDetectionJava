package com.anomalydetection.contracts.safety;

import com.anomalydetection.shared.safety.LifecycleStage;

public record AddVerificationDto(
    LifecycleStage stage,
    String method,
    String result,
    String toolRef,
    String notes) {}
