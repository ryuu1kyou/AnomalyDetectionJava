package com.anomalydetection.contracts.anomalydetection;

import com.anomalydetection.domain.anomalydetection.AnomalyType;
import com.anomalydetection.domain.anomalydetection.AsilLevel;
import com.anomalydetection.domain.anomalydetection.ImplementationType;
import com.anomalydetection.domain.anomalydetection.LogicComplexity;
import com.anomalydetection.domain.anomalydetection.SharingLevel;
import java.util.UUID;

public record CreateUpdateCanAnomalyDetectionLogicDto(
    String name,
    String version,
    String oemCode,
    AnomalyType anomalyType,
    String description,
    String targetSystemType,
    LogicComplexity complexity,
    String requirements,
    ImplementationType implementationType,
    String implementationContent,
    String implementationLanguage,
    String implementationEntryPoint,
    AsilLevel asilLevel,
    String safetyRequirementId,
    String safetyGoalId,
    String hazardAnalysisId,
    SharingLevel sharingLevel,
    UUID vehiclePhaseId) {}
