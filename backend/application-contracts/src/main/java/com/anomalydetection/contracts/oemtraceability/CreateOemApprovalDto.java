package com.anomalydetection.contracts.oemtraceability;

import com.anomalydetection.domain.oemtraceability.OemApprovalType;

public record CreateOemApprovalDto(
    String entityId,
    String entityType,
    String oemCode,
    OemApprovalType type,
    String approvalReason,
    String dueDate,
    int priority) {}
