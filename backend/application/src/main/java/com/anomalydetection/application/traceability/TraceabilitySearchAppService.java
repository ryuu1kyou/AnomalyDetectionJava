package com.anomalydetection.application.traceability;

import com.anomalydetection.application.oemtraceability.OemTraceabilityAppService;
import com.anomalydetection.application.safety.SafetyTraceAppService;
import com.anomalydetection.contracts.oemtraceability.OemTraceabilityPermissions;
import com.anomalydetection.contracts.safety.FeatureTraceabilityDto;
import com.anomalydetection.contracts.safety.SafetyTracePermissions;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cross-module search: aggregates Safety records and OEM approvals by feature_id.
 * Automotive-safety Phase B — traceability ledger cross-reference.
 *
 * NOTE: inner service calls carry their own @PreAuthorize (Spring AOP proxy).
 * A future refactoring can inject repositories + a shared mapper to unify checks.
 */
@Service
@Transactional(readOnly = true)
@PreAuthorize(
    "hasAuthority('" + SafetyTracePermissions.DEFAULT + "')"
    + " and hasAuthority('" + OemTraceabilityPermissions.APPROVAL_DEFAULT + "')")
public class TraceabilitySearchAppService {

  private final SafetyTraceAppService safetyService;
  private final OemTraceabilityAppService oemService;

  public TraceabilitySearchAppService(
      SafetyTraceAppService safetyService,
      OemTraceabilityAppService oemService) {
    this.safetyService = safetyService;
    this.oemService = oemService;
  }

  public FeatureTraceabilityDto findByFeatureId(String featureId) {
    var safetyRecords = safetyService.findByFeatureId(featureId);
    var oemApprovals = oemService.findApprovalsByFeatureId(featureId);
    return new FeatureTraceabilityDto(featureId, safetyRecords, oemApprovals);
  }
}
