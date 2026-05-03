package com.anomalydetection.contracts.safety;

import com.anomalydetection.domain.safety.SafetyApprovalStatus;
import com.anomalydetection.shared.safety.DocSyncStatus;
import com.anomalydetection.shared.safety.IfImpact;
import com.anomalydetection.shared.safety.TraceabilityScope;
import java.util.List;

public record SafetyTraceRecordDto(
    String id,
    String name,
    String description,
    String requirementId,
    String safetyGoalId,
    String hazardAnalysisId,
    String asilLevel,
    String detectionLogicId,
    String projectId,
    String version,
    SafetyApprovalStatus approvalStatus,
    String submittedAt,
    String approvedAt,
    String rejectedAt,
    String approvalComments,
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
