package com.anomalydetection.web.anomalydetection;

import com.anomalydetection.application.anomalydetection.AnomalyDetectionResultAppService;
import com.anomalydetection.contracts.anomalydetection.AnomalyDetectionResultDto;
import com.anomalydetection.contracts.anomalydetection.CreateAnomalyDetectionResultDto;
import com.anomalydetection.domain.anomalydetection.AnomalyLevel;
import com.anomalydetection.domain.anomalydetection.SharingLevel;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/anomaly-detection-results")
public class AnomalyDetectionResultController {

  private final AnomalyDetectionResultAppService appService;

  public AnomalyDetectionResultController(AnomalyDetectionResultAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public List<AnomalyDetectionResultDto> getList(
      @RequestParam(required = false) AnomalyLevel anomalyLevel) {
    if (anomalyLevel != null) return appService.getByAnomalyLevel(anomalyLevel);
    return appService.getList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<AnomalyDetectionResultDto> get(@PathVariable UUID id) {
    return appService.getById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/unresolved")
  public List<AnomalyDetectionResultDto> getUnresolved() {
    return appService.getUnresolved();
  }

  @PostMapping
  public AnomalyDetectionResultDto create(@RequestBody CreateAnomalyDetectionResultDto input) {
    return appService.create(input);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return appService.delete(id) ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }

  @PostMapping("/{id}/resolve")
  public ResponseEntity<AnomalyDetectionResultDto> resolve(
      @PathVariable UUID id,
      @RequestParam(required = false) UUID resolvedBy,
      @RequestParam String notes) {
    return appService.resolve(id, resolvedBy, notes).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/mark-false-positive")
  public ResponseEntity<AnomalyDetectionResultDto> markAsFalsePositive(
      @PathVariable UUID id,
      @RequestParam(required = false) UUID resolvedBy,
      @RequestParam String reason) {
    return appService.markAsFalsePositive(id, resolvedBy, reason).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/share")
  public ResponseEntity<AnomalyDetectionResultDto> share(
      @PathVariable UUID id,
      @RequestParam SharingLevel level,
      @RequestParam(required = false) UUID sharedBy) {
    return appService.share(id, level, sharedBy).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
