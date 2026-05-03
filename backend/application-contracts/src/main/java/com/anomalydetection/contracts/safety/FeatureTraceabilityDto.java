package com.anomalydetection.contracts.safety;

import com.anomalydetection.contracts.oemtraceability.OemApprovalDto;
import java.util.List;

/**
 * Cross-module response DTO: aggregates all Safety trace records and OEM approvals
 * that share the same {@code featureId} (automotive-safety Phase B).
 */
public record FeatureTraceabilityDto(
    String featureId,
    List<SafetyTraceRecordDto> safetyRecords,
    List<OemApprovalDto> oemApprovals) {}
