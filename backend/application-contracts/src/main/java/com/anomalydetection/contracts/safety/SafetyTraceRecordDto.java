package com.anomalydetection.contracts.safety;

import com.anomalydetection.domain.safety.SafetyApprovalStatus;
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
    String approvalComments,
    List<String> relatedDocuments) {}
