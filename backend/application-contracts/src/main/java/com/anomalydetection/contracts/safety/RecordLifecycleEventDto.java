package com.anomalydetection.contracts.safety;

import com.anomalydetection.shared.safety.LifecycleStage;

public record RecordLifecycleEventDto(
    LifecycleStage stage,
    String eventType,
    String description) {}
