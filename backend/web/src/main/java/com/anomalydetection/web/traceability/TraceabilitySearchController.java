package com.anomalydetection.web.traceability;

import com.anomalydetection.application.traceability.TraceabilitySearchAppService;
import com.anomalydetection.contracts.safety.FeatureTraceabilityDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cross-module traceability search API (automotive-safety Phase B).
 * Aggregates Safety records and OEM approvals by feature_id.
 */
@RestController
@RequestMapping("/api/app/traceability")
public class TraceabilitySearchController {

  private final TraceabilitySearchAppService searchService;

  public TraceabilitySearchController(TraceabilitySearchAppService searchService) {
    this.searchService = searchService;
  }

  /**
   * GET /api/app/traceability/feature/{featureId}
   * Returns all Safety trace records and OEM approvals that share the given featureId.
   */
  @GetMapping("/feature/{featureId}")
  public ResponseEntity<FeatureTraceabilityDto> getByFeatureId(
      @PathVariable String featureId) {
    return ResponseEntity.ok(searchService.findByFeatureId(featureId));
  }
}
