package com.anomalydetection.contracts.safety;

import java.util.List;

public record UpdateSafetyTraceRecordDto(
    String name,
    String description,
    String requirementId,
    String safetyGoalId,
    String hazardAnalysisId,
    String asilLevel,
    String detectionLogicId,
    String projectId,
    List<String> relatedDocuments) {}
