package com.anomalydetection.web.safety;

import com.anomalydetection.application.safety.SafetyTraceAppService;
import com.anomalydetection.contracts.safety.AddChangeRequestDto;
import com.anomalydetection.contracts.safety.AddValidationDto;
import com.anomalydetection.contracts.safety.AddVerificationDto;
import com.anomalydetection.contracts.safety.ChangeRequestRecordDto;
import com.anomalydetection.contracts.safety.CreateSafetyTraceLinkDto;
import com.anomalydetection.contracts.safety.CreateSafetyTraceRecordDto;
import com.anomalydetection.contracts.safety.GetSafetyTraceInput;
import com.anomalydetection.contracts.safety.LifecycleEventDto;
import com.anomalydetection.contracts.safety.RecordLifecycleEventDto;
import com.anomalydetection.contracts.safety.SafetyTraceLinkDto;
import com.anomalydetection.contracts.safety.SafetyTraceRecordDto;
import com.anomalydetection.contracts.safety.UpdateSafetyTraceRecordDto;
import com.anomalydetection.contracts.safety.ValidationRecordDto;
import com.anomalydetection.contracts.safety.VerificationRecordDto;
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
@RequestMapping("/api/app/safety-trace-records")
public class SafetyTraceController {

  private final SafetyTraceAppService appService;

  public SafetyTraceController(SafetyTraceAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public List<SafetyTraceRecordDto> getList(
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) String asilLevel,
      @RequestParam(required = false) String approvalStatus,
      @RequestParam(required = false) String projectId,
      @RequestParam(required = false) String detectionLogicId,
      @RequestParam(required = false) Integer skipCount,
      @RequestParam(required = false) Integer maxResultCount) {
    return appService.getList(new GetSafetyTraceInput(
        filter, asilLevel, approvalStatus, projectId, detectionLogicId,
        skipCount, maxResultCount));
  }

  @GetMapping("/{id}")
  public ResponseEntity<SafetyTraceRecordDto> get(@PathVariable UUID id) {
    return appService.getById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public SafetyTraceRecordDto create(@RequestBody CreateSafetyTraceRecordDto input) {
    return appService.create(input);
  }

  @PutMapping("/{id}")
  public ResponseEntity<SafetyTraceRecordDto> update(
      @PathVariable UUID id, @RequestBody UpdateSafetyTraceRecordDto input) {
    return appService.update(id, input).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return appService.delete(id) ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }

  @PostMapping("/{id}/submit")
  public ResponseEntity<SafetyTraceRecordDto> submit(@PathVariable UUID id) {
    return appService.submit(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/approve")
  public ResponseEntity<SafetyTraceRecordDto> approve(
      @PathVariable UUID id,
      @RequestParam(required = false) String comments) {
    return appService.approve(id, comments).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/reject")
  public ResponseEntity<SafetyTraceRecordDto> reject(
      @PathVariable UUID id,
      @RequestParam String comments) {
    return appService.reject(id, comments).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  // ── V&V / Lifecycle endpoints (M9-B) ────────────────────────────────────────

  @GetMapping("/{id}/verifications")
  public List<VerificationRecordDto> getVerifications(@PathVariable UUID id) {
    return appService.getVerifications(id);
  }

  @PostMapping("/{id}/verifications")
  public ResponseEntity<VerificationRecordDto> addVerification(
      @PathVariable UUID id, @RequestBody AddVerificationDto input) {
    return appService.addVerification(id, input).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/validations")
  public List<ValidationRecordDto> getValidations(@PathVariable UUID id) {
    return appService.getValidations(id);
  }

  @PostMapping("/{id}/validations")
  public ResponseEntity<ValidationRecordDto> addValidation(
      @PathVariable UUID id, @RequestBody AddValidationDto input) {
    return appService.addValidation(id, input).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/lifecycle-events")
  public List<LifecycleEventDto> getLifecycleEvents(@PathVariable UUID id) {
    return appService.getLifecycleEvents(id);
  }

  @PostMapping("/{id}/lifecycle-events")
  public ResponseEntity<LifecycleEventDto> recordLifecycleEvent(
      @PathVariable UUID id, @RequestBody RecordLifecycleEventDto input) {
    return appService.recordLifecycleEvent(id, input).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/change-requests")
  public List<ChangeRequestRecordDto> getChangeRequests(@PathVariable UUID id) {
    return appService.getChangeRequests(id);
  }

  @PostMapping("/{id}/change-requests")
  public ResponseEntity<ChangeRequestRecordDto> addChangeRequest(
      @PathVariable UUID id, @RequestBody AddChangeRequestDto input) {
    return appService.addChangeRequest(id, input).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/links")
  public List<SafetyTraceLinkDto> getLinks(@PathVariable UUID id) {
    return appService.getLinks(id);
  }

  @PostMapping("/links")
  public SafetyTraceLinkDto createLink(@RequestBody CreateSafetyTraceLinkDto input) {
    return appService.createLink(input);
  }

  @DeleteMapping("/links/{id}")
  public ResponseEntity<Void> deleteLink(@PathVariable UUID id) {
    return appService.deleteLink(id) ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}
