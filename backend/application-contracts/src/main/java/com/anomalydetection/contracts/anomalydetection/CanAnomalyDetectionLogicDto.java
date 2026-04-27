package com.anomalydetection.contracts.anomalydetection;

import com.anomalydetection.domain.anomalydetection.AnomalyType;
import com.anomalydetection.domain.anomalydetection.AsilLevel;
import com.anomalydetection.domain.anomalydetection.DetectionLogicStatus;
import com.anomalydetection.domain.anomalydetection.ImplementationType;
import com.anomalydetection.domain.anomalydetection.LogicComplexity;
import com.anomalydetection.domain.anomalydetection.SharingLevel;
import java.time.Instant;
import java.util.UUID;

public record CanAnomalyDetectionLogicDto(
    UUID id,
    UUID tenantId,
    String name,
    String version,
    String oemCode,
    AnomalyType anomalyType,
    String description,
    String targetSystemType,
    LogicComplexity complexity,
    String requirements,
    ImplementationType implementationType,
    String implementationLanguage,
    AsilLevel asilLevel,
    String safetyRequirementId,
    String safetyGoalId,
    DetectionLogicStatus status,
    SharingLevel sharingLevel,
    UUID vehiclePhaseId,
    Instant approvedAt,
    UUID approvedBy,
    String approvalNotes,
    int executionCount,
    Instant lastExecutedAt,
    Double lastExecutionTimeMs) {}
