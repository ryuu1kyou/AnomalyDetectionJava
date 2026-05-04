package com.anomalydetection.contracts.safety;

import com.anomalydetection.shared.safety.ChangeApprovalStatus;
import com.anomalydetection.shared.safety.ChangeType;

public record ChangeRequestRecordDto(
    String id,
    String changeId,
    ChangeType changeType,
    String description,
    String rationale,
    ChangeApprovalStatus status,
    String requestedBy,
    String requestedAt,
    String reviewedBy,
    String reviewedAt,
    String reviewComments) {}
