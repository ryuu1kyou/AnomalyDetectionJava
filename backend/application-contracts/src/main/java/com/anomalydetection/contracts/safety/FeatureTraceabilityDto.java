package com.anomalydetection.contracts.safety;

import com.anomalydetection.contracts.oemtraceability.OemApprovalDto;
import java.util.List;

/**
 * Cross-module response DTO: aggregates all Safety trace records, OEM approvals,
 * and design-intent decisions that share the same {@code featureId}.
 */
public record FeatureTraceabilityDto(
    String featureId,
    List<SafetyTraceRecordDto> safetyRecords,
    List<OemApprovalDto> oemApprovals,
    List<DecisionLedgerDto> decisions) {}
