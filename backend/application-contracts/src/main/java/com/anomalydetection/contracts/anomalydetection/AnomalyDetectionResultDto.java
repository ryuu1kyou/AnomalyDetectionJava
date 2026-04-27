package com.anomalydetection.contracts.anomalydetection;

import com.anomalydetection.domain.anomalydetection.AnomalyLevel;
import com.anomalydetection.domain.anomalydetection.AnomalyType;
import com.anomalydetection.domain.anomalydetection.ResolutionStatus;
import com.anomalydetection.domain.anomalydetection.SharingLevel;
import java.time.Instant;
import java.util.UUID;

public record AnomalyDetectionResultDto(
    UUID id,
    UUID tenantId,
    UUID detectionLogicId,
    UUID canSignalId,
    Instant detectedAt,
    AnomalyLevel anomalyLevel,
    AnomalyType anomalyType,
    double confidenceScore,
    String description,
    Double signalValue,
    Instant inputTimestamp,
    AnomalyType detectionType,
    String triggerCondition,
    Double executionTimeMs,
    boolean isValidated,
    boolean isFalsePositive,
    String detectionCondition,
    Long detectionDurationMs,
    ResolutionStatus resolutionStatus,
    Instant resolvedAt,
    UUID resolvedBy,
    String resolutionNotes,
    SharingLevel sharingLevel,
    boolean isShared) {}
