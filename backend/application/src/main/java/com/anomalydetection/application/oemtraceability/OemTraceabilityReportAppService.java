package com.anomalydetection.application.oemtraceability;

import com.anomalydetection.contracts.oemtraceability.OemApprovalDto;
import com.anomalydetection.contracts.oemtraceability.OemStatusSummaryDto;
import com.anomalydetection.contracts.oemtraceability.OemTraceabilityPermissions;
import com.anomalydetection.contracts.oemtraceability.OemTraceabilityReportDto;
import com.anomalydetection.domain.oemtraceability.OemApprovalStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * M9-C: Cross-OEM traceability report — shows per-OEM approval status breakdown
 * for a given feature_id and identifies OEMs that diverge from the majority status.
 */
@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.APPROVAL_DEFAULT + "')")
public class OemTraceabilityReportAppService {

  private final OemTraceabilityAppService oemService;

  public OemTraceabilityReportAppService(OemTraceabilityAppService oemService) {
    this.oemService = oemService;
  }

  public OemTraceabilityReportDto reportByFeatureId(String featureId) {
    var approvals = oemService.findApprovalsByFeatureId(featureId);

    // Group by OEM code
    Map<String, List<OemApprovalDto>> byOem = approvals.stream()
        .collect(Collectors.groupingBy(
            a -> a.oemCode() != null ? a.oemCode() : "UNKNOWN",
            LinkedHashMap::new,
            Collectors.toList()));

    var summaries = new ArrayList<OemStatusSummaryDto>();
    int totalApproved = 0;
    int totalPending = 0;
    int totalRejected = 0;

    for (var entry : byOem.entrySet()) {
      String oemCode = entry.getKey();
      List<OemApprovalDto> oemApprovals = entry.getValue();

      int approved = (int) oemApprovals.stream()
          .filter(a -> a.status() == OemApprovalStatus.APPROVED).count();
      int pending = (int) oemApprovals.stream()
          .filter(a -> a.status() == OemApprovalStatus.PENDING).count();
      int rejected = (int) oemApprovals.stream()
          .filter(a -> a.status() == OemApprovalStatus.REJECTED
              || a.status() == OemApprovalStatus.CANCELLED).count();

      totalApproved += approved;
      totalPending += pending;
      totalRejected += rejected;

      String dominant = dominant(approved, pending, rejected);
      summaries.add(new OemStatusSummaryDto(
          oemCode, oemApprovals.size(), approved, pending, rejected, dominant, oemApprovals));
    }

    // Majority status across all OEMs
    String majorityStatus = dominant(totalApproved, totalPending, totalRejected);

    // Divergent OEMs: those whose dominant status differs from the overall majority
    List<String> divergent = summaries.stream()
        .filter(s -> !s.dominantStatus().equals(majorityStatus))
        .map(OemStatusSummaryDto::oemCode)
        .sorted()
        .toList();

    return new OemTraceabilityReportDto(
        featureId,
        byOem.size(),
        totalApproved,
        totalPending,
        totalRejected,
        summaries,
        divergent);
  }

  private static String dominant(int approved, int pending, int rejected) {
    if (approved >= pending && approved >= rejected) return "APPROVED";
    if (pending >= approved && pending >= rejected) return "PENDING";
    return "REJECTED";
  }
}
