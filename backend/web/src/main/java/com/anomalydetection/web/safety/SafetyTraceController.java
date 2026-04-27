package com.anomalydetection.web.safety;

import com.anomalydetection.application.safety.SafetyTraceAppService;
import com.anomalydetection.contracts.safety.CreateSafetyTraceLinkDto;
import com.anomalydetection.contracts.safety.CreateSafetyTraceRecordDto;
import com.anomalydetection.contracts.safety.GetSafetyTraceInput;
import com.anomalydetection.contracts.safety.SafetyTraceLinkDto;
import com.anomalydetection.contracts.safety.SafetyTraceRecordDto;
import com.anomalydetection.contracts.safety.UpdateSafetyTraceRecordDto;
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
