package com.anomalydetection.web.anomalydetection;

import com.anomalydetection.application.anomalydetection.CanAnomalyDetectionLogicAppService;
import com.anomalydetection.contracts.anomalydetection.CanAnomalyDetectionLogicDto;
import com.anomalydetection.contracts.anomalydetection.CreateUpdateCanAnomalyDetectionLogicDto;
import com.anomalydetection.domain.anomalydetection.DetectionLogicStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/can-anomaly-detection-logics")
public class CanAnomalyDetectionLogicController {

  private final CanAnomalyDetectionLogicAppService appService;

  public CanAnomalyDetectionLogicController(CanAnomalyDetectionLogicAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public List<CanAnomalyDetectionLogicDto> getList(
      @RequestParam(required = false) DetectionLogicStatus status) {
    if (status != null) return appService.getByStatus(status);
    return appService.getList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CanAnomalyDetectionLogicDto> get(@PathVariable UUID id) {
    return appService.getById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public CanAnomalyDetectionLogicDto create(
      @RequestBody CreateUpdateCanAnomalyDetectionLogicDto input) {
    return appService.create(input);
  }

  @PutMapping("/{id}")
  public ResponseEntity<CanAnomalyDetectionLogicDto> update(
      @PathVariable UUID id, @RequestBody CreateUpdateCanAnomalyDetectionLogicDto input) {
    return appService.update(id, input).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return appService.delete(id) ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }

  @PostMapping("/{id}/submit-for-approval")
  public ResponseEntity<CanAnomalyDetectionLogicDto> submitForApproval(@PathVariable UUID id) {
    return appService.submitForApproval(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/approve")
  public ResponseEntity<CanAnomalyDetectionLogicDto> approve(
      @PathVariable UUID id,
      @RequestParam(required = false) String notes) {
    return appService.approve(id, null, notes).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/reject")
  public ResponseEntity<CanAnomalyDetectionLogicDto> reject(
      @PathVariable UUID id, @RequestParam String reason) {
    return appService.reject(id, reason).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/deprecate")
  public ResponseEntity<CanAnomalyDetectionLogicDto> deprecate(
      @PathVariable UUID id, @RequestParam String reason) {
    return appService.deprecate(id, reason).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
