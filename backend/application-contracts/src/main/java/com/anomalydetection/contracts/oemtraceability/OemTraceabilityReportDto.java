package com.anomalydetection.contracts.oemtraceability;

import java.util.List;

/**
 * Cross-OEM traceability report for a single feature_id.
 * Shows per-OEM approval status breakdown and identifies divergent OEMs.
 */
public record OemTraceabilityReportDto(
    String featureId,
    int totalOems,
    int approvedCount,
    int pendingCount,
    int rejectedCount,
    List<OemStatusSummaryDto> byOem,
    List<String> divergentOems) {}
