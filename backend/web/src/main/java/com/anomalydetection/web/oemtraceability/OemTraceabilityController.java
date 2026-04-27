package com.anomalydetection.web.oemtraceability;

import com.anomalydetection.application.oemtraceability.OemTraceabilityAppService;
import com.anomalydetection.contracts.oemtraceability.CreateOemApprovalDto;
import com.anomalydetection.contracts.oemtraceability.CreateOemCustomizationDto;
import com.anomalydetection.contracts.oemtraceability.OemApprovalDto;
import com.anomalydetection.contracts.oemtraceability.OemCustomizationDto;
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
@RequestMapping("/api/app/oem-traceability")
public class OemTraceabilityController {

  private final OemTraceabilityAppService appService;

  public OemTraceabilityController(OemTraceabilityAppService appService) {
    this.appService = appService;
  }

  // --- Approvals ---

  @GetMapping("/approvals")
  public List<OemApprovalDto> getApprovals(
      @RequestParam(required = false) String oemCode,
      @RequestParam(required = false) String status) {
    return appService.getApprovals(oemCode, status);
  }

  @GetMapping("/approvals/{id}")
  public ResponseEntity<OemApprovalDto> getApproval(@PathVariable UUID id) {
    return appService.getApprovalById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/approvals")
  public OemApprovalDto createApproval(@RequestBody CreateOemApprovalDto input) {
    return appService.createApproval(input);
  }

  @PostMapping("/approvals/{id}/approve")
  public ResponseEntity<OemApprovalDto> approve(
      @PathVariable UUID id, @RequestParam(required = false) String notes) {
    return appService.approveApproval(id, notes).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/approvals/{id}/reject")
  public ResponseEntity<OemApprovalDto> reject(
      @PathVariable UUID id, @RequestParam String notes) {
    return appService.rejectApproval(id, notes).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/approvals/{id}/cancel")
  public ResponseEntity<OemApprovalDto> cancel(
      @PathVariable UUID id, @RequestParam String reason) {
    return appService.cancelApproval(id, reason).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/approvals/{id}")
  public ResponseEntity<Void> deleteApproval(@PathVariable UUID id) {
    return appService.deleteApproval(id) ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }

  // --- Customizations ---

  @GetMapping("/customizations")
  public List<OemCustomizationDto> getCustomizations(
      @RequestParam(required = false) String oemCode,
      @RequestParam(required = false) String status) {
    return appService.getCustomizations(oemCode, status);
  }

  @GetMapping("/customizations/{id}")
  public ResponseEntity<OemCustomizationDto> getCustomization(@PathVariable UUID id) {
    return appService.getCustomizationById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/customizations")
  public OemCustomizationDto createCustomization(@RequestBody CreateOemCustomizationDto input) {
    return appService.createCustomization(input);
  }

  @PostMapping("/customizations/{id}/submit")
  public ResponseEntity<OemCustomizationDto> submit(@PathVariable UUID id) {
    return appService.submitCustomization(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/customizations/{id}/approve")
  public ResponseEntity<OemCustomizationDto> approveCustomization(
      @PathVariable UUID id, @RequestParam(required = false) String notes) {
    return appService.approveCustomization(id, notes).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/customizations/{id}/reject")
  public ResponseEntity<OemCustomizationDto> rejectCustomization(
      @PathVariable UUID id, @RequestParam String notes) {
    return appService.rejectCustomization(id, notes).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/customizations/{id}")
  public ResponseEntity<Void> deleteCustomization(@PathVariable UUID id) {
    return appService.deleteCustomization(id) ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}
