package com.anomalydetection.contracts.oemtraceability;

import com.anomalydetection.domain.oemtraceability.OemApprovalStatus;
import com.anomalydetection.domain.oemtraceability.OemApprovalType;

public record OemApprovalDto(
    String id,
    String entityId,
    String entityType,
    String oemCode,
    OemApprovalType type,
    String requestedBy,
    String requestedAt,
    String approvedBy,
    String approvedAt,
    OemApprovalStatus status,
    String approvalReason,
    String approvalNotes,
    String dueDate,
    int priority,
    boolean isOverdue) {}
