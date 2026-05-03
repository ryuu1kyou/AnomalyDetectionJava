package com.anomalydetection.contracts.oemtraceability;

import com.anomalydetection.domain.oemtraceability.OemApprovalType;

public record CreateOemApprovalDto(
    String entityId,
    String entityType,
    String oemCode,
    OemApprovalType type,
    String approvalReason,
    String dueDate,
    int priority,
    // Traceability keys
    String featureId,
    String decisionId,
    String applicability,
    String confidentialityLevel) {}
