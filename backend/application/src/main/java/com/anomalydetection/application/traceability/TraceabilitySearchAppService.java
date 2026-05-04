package com.anomalydetection.application.traceability;

import com.anomalydetection.application.oemtraceability.OemTraceabilityAppService;
import com.anomalydetection.application.safety.DecisionLedgerAppService;
import com.anomalydetection.application.safety.SafetyTraceAppService;
import com.anomalydetection.contracts.oemtraceability.OemTraceabilityPermissions;
import com.anomalydetection.contracts.safety.DecisionLedgerPermissions;
import com.anomalydetection.contracts.safety.FeatureTraceabilityDto;
import com.anomalydetection.contracts.safety.SafetyTracePermissions;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cross-module search: aggregates Safety records, OEM approvals, and design-intent
 * decisions by feature_id. Automotive-safety Phase B + M9-A.
 */
@Service
@Transactional(readOnly = true)
@PreAuthorize(
    "hasAuthority('" + SafetyTracePermissions.DEFAULT + "')"
    + " and hasAuthority('" + OemTraceabilityPermissions.APPROVAL_DEFAULT + "')"
    + " and hasAuthority('" + DecisionLedgerPermissions.DEFAULT + "')")
public class TraceabilitySearchAppService {

  private final SafetyTraceAppService safetyService;
  private final OemTraceabilityAppService oemService;
  private final DecisionLedgerAppService decisionService;

  public TraceabilitySearchAppService(
      SafetyTraceAppService safetyService,
      OemTraceabilityAppService oemService,
      DecisionLedgerAppService decisionService) {
    this.safetyService = safetyService;
    this.oemService = oemService;
    this.decisionService = decisionService;
  }

  public FeatureTraceabilityDto findByFeatureId(String featureId) {
    var safetyRecords = safetyService.findByFeatureId(featureId);
    var oemApprovals = oemService.findApprovalsByFeatureId(featureId);
    var decisions = decisionService.findByFeatureId(featureId);
    return new FeatureTraceabilityDto(featureId, safetyRecords, oemApprovals, decisions);
  }
}
