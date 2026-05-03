package com.anomalydetection.contracts.safety;

import com.anomalydetection.domain.safety.DocSyncStatus;
import com.anomalydetection.domain.safety.IfImpact;
import com.anomalydetection.domain.safety.TraceabilityScope;
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
    List<String> relatedDocuments,
    // Traceability keys
    String featureId,
    String decisionId,
    String changeId,
    IfImpact ifImpact,
    String unknownUntil,
    String unknownOwnerId,
    String designRationale,
    String assumption,
    String constraintText,
    DocSyncStatus docSyncStatus,
    TraceabilityScope scope,
    String applicability) {}
