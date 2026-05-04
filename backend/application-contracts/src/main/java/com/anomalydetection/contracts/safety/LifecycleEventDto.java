package com.anomalydetection.contracts.safety;

import com.anomalydetection.shared.safety.LifecycleStage;

public record LifecycleEventDto(
    String id,
    LifecycleStage stage,
    String eventType,
    String description,
    String occurredAt,
    String recordedBy) {}
