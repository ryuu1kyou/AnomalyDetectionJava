package com.anomalydetection.contracts.oemtraceability;

import java.util.List;

/** Per-OEM approval breakdown within a feature-level cross-OEM report. */
public record OemStatusSummaryDto(
    String oemCode,
    int totalApprovals,
    int approvedCount,
    int pendingCount,
    int rejectedCount,
    String dominantStatus,
    List<OemApprovalDto> approvals) {}
