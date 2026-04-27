package com.anomalydetection.contracts.anomalydetection;

import com.anomalydetection.domain.anomalydetection.AnomalyLevel;
import com.anomalydetection.domain.anomalydetection.AnomalyType;
import java.time.Instant;
import java.util.UUID;

public record CreateAnomalyDetectionResultDto(
    UUID detectionLogicId,
    UUID canSignalId,
    AnomalyLevel anomalyLevel,
    AnomalyType anomalyType,
    double confidenceScore,
    String description,
    Double signalValue,
    Instant inputTimestamp,
    AnomalyType detectionType,
    String triggerCondition,
    Double executionTimeMs,
    String detectionCondition,
    Long detectionDurationMs) {}
