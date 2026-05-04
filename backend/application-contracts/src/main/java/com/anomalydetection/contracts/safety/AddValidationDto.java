package com.anomalydetection.contracts.safety;

import com.anomalydetection.shared.safety.LifecycleStage;

public record AddValidationDto(
    LifecycleStage stage,
    String scenario,
    String outcome,
    String testRef,
    String notes) {}
