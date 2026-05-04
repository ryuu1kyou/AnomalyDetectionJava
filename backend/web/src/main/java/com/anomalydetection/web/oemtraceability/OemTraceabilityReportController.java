package com.anomalydetection.web.oemtraceability;

import com.anomalydetection.application.oemtraceability.OemTraceabilityReportAppService;
import com.anomalydetection.contracts.oemtraceability.OemTraceabilityReportDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/oem-traceability-report")
public class OemTraceabilityReportController {

  private final OemTraceabilityReportAppService reportService;

  public OemTraceabilityReportController(OemTraceabilityReportAppService reportService) {
    this.reportService = reportService;
  }

  @GetMapping("/by-feature")
  public OemTraceabilityReportDto reportByFeature(@RequestParam String featureId) {
    return reportService.reportByFeatureId(featureId);
  }
}
