package com.anomalydetection.application.oemtraceability;

import com.anomalydetection.contracts.oemtraceability.CreateOemApprovalDto;
import com.anomalydetection.contracts.oemtraceability.CreateOemCustomizationDto;
import com.anomalydetection.contracts.oemtraceability.OemApprovalDto;
import com.anomalydetection.contracts.oemtraceability.OemCustomizationDto;
import com.anomalydetection.contracts.oemtraceability.OemTraceabilityPermissions;
import com.anomalydetection.domain.oemtraceability.OemApproval;
import com.anomalydetection.domain.oemtraceability.OemApprovalRepository;
import com.anomalydetection.domain.oemtraceability.OemApprovalStatus;
import com.anomalydetection.domain.oemtraceability.OemCustomization;
import com.anomalydetection.domain.oemtraceability.OemCustomizationRepository;
import com.anomalydetection.domain.oemtraceability.OemCustomizationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OemTraceabilityAppService {

  private final OemApprovalRepository approvalRepo;
  private final OemCustomizationRepository customizationRepo;

  public OemTraceabilityAppService(
      OemApprovalRepository approvalRepo,
      OemCustomizationRepository customizationRepo) {
    this.approvalRepo = approvalRepo;
    this.customizationRepo = customizationRepo;
  }

  // --- Approvals ---

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.APPROVAL_DEFAULT + "')")
  public List<OemApprovalDto> getApprovals(String oemCode, String status) {
    List<OemApproval> results;
    if (status != null && !status.isBlank()) {
      try {
        results = approvalRepo.findAllByStatus(OemApprovalStatus.valueOf(status));
      } catch (IllegalArgumentException e) {
        results = approvalRepo.findAll();
      }
    } else if (oemCode != null && !oemCode.isBlank()) {
      results = approvalRepo.findAllByOemCode(oemCode);
    } else {
      results = approvalRepo.findAll();
    }
    return results.stream().map(this::toApprovalDto).toList();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.APPROVAL_DEFAULT + "')")
  public Optional<OemApprovalDto> getApprovalById(UUID id) {
    return approvalRepo.findById(id).map(this::toApprovalDto);
  }

  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.APPROVAL_CREATE + "')")
  public OemApprovalDto createApproval(CreateOemApprovalDto input) {
    var approval = new OemApproval(UUID.randomUUID(), input.entityId(), input.entityType(),
        input.oemCode(), input.type());
    approval.setApprovalReason(input.approvalReason());
    approval.setPriority(input.priority());
    if (input.dueDate() != null && !input.dueDate().isBlank())
      approval.setDueDate(Instant.parse(input.dueDate()));
    // Traceability keys
    if (input.featureId() != null) approval.setFeatureId(input.featureId());
    if (input.decisionId() != null) approval.setDecisionId(input.decisionId());
    if (input.applicability() != null) approval.setApplicability(input.applicability());
    if (input.confidentialityLevel() != null) approval.setConfidentialityLevel(input.confidentialityLevel());
    return toApprovalDto(approvalRepo.save(approval));
  }

  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.APPROVAL_MANAGE + "')")
  public Optional<OemApprovalDto> approveApproval(UUID id, String notes) {
    return approvalRepo.findById(id).map(a -> {
      a.approve(null, notes);
      return toApprovalDto(approvalRepo.save(a));
    });
  }

  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.APPROVAL_MANAGE + "')")
  public Optional<OemApprovalDto> rejectApproval(UUID id, String notes) {
    return approvalRepo.findById(id).map(a -> {
      a.reject(null, notes);
      return toApprovalDto(approvalRepo.save(a));
    });
  }

  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.APPROVAL_MANAGE + "')")
  public Optional<OemApprovalDto> cancelApproval(UUID id, String reason) {
    return approvalRepo.findById(id).map(a -> {
      a.cancel(null, reason);
      return toApprovalDto(approvalRepo.save(a));
    });
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.APPROVAL_DEFAULT + "')")
  public List<OemApprovalDto> findApprovalsByFeatureId(String featureId) {
    return approvalRepo.findAllByFeatureId(featureId).stream().map(this::toApprovalDto).toList();
  }

  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.APPROVAL_MANAGE + "')")
  public boolean deleteApproval(UUID id) {
    if (!approvalRepo.existsById(id)) return false;
    approvalRepo.deleteById(id);
    return true;
  }

  // --- Customizations ---

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.CUSTOMIZATION_DEFAULT + "')")
  public List<OemCustomizationDto> getCustomizations(String oemCode, String status) {
    List<OemCustomization> results;
    if (status != null && !status.isBlank()) {
      try {
        results = customizationRepo.findAllByStatus(OemCustomizationStatus.valueOf(status));
      } catch (IllegalArgumentException e) {
        results = customizationRepo.findAll();
      }
    } else if (oemCode != null && !oemCode.isBlank()) {
      results = customizationRepo.findAllByOemCode(oemCode);
    } else {
      results = customizationRepo.findAll();
    }
    return results.stream().map(this::toCustomizationDto).toList();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.CUSTOMIZATION_DEFAULT + "')")
  public Optional<OemCustomizationDto> getCustomizationById(UUID id) {
    return customizationRepo.findById(id).map(this::toCustomizationDto);
  }

  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.CUSTOMIZATION_CREATE + "')")
  public OemCustomizationDto createCustomization(CreateOemCustomizationDto input) {
    var c = new OemCustomization(UUID.randomUUID(), input.entityId(), input.entityType(),
        input.oemCode(), input.type());
    c.setCustomParameters(input.customParameters());
    c.setOriginalParameters(input.originalParameters());
    c.setCustomizationReason(input.customizationReason());
    return toCustomizationDto(customizationRepo.save(c));
  }

  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.CUSTOMIZATION_MANAGE + "')")
  public Optional<OemCustomizationDto> submitCustomization(UUID id) {
    return customizationRepo.findById(id).map(c -> {
      c.submitForApproval();
      return toCustomizationDto(customizationRepo.save(c));
    });
  }

  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.CUSTOMIZATION_MANAGE + "')")
  public Optional<OemCustomizationDto> approveCustomization(UUID id, String notes) {
    return customizationRepo.findById(id).map(c -> {
      c.approve(null, notes);
      return toCustomizationDto(customizationRepo.save(c));
    });
  }

  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.CUSTOMIZATION_MANAGE + "')")
  public Optional<OemCustomizationDto> rejectCustomization(UUID id, String notes) {
    return customizationRepo.findById(id).map(c -> {
      c.reject(null, notes);
      return toCustomizationDto(customizationRepo.save(c));
    });
  }

  @PreAuthorize("hasAuthority('" + OemTraceabilityPermissions.CUSTOMIZATION_MANAGE + "')")
  public boolean deleteCustomization(UUID id) {
    if (!customizationRepo.existsById(id)) return false;
    customizationRepo.deleteById(id);
    return true;
  }

  // --- Mappers ---

  private OemApprovalDto toApprovalDto(OemApproval a) {
    return new OemApprovalDto(
        a.getId().toString(),
        a.getEntityId(),
        a.getEntityType(),
        a.getOemCode(),
        a.getType(),
        a.getRequestedBy() != null ? a.getRequestedBy().toString() : null,
        a.getRequestedAt() != null ? a.getRequestedAt().toString() : null,
        a.getApprovedBy() != null ? a.getApprovedBy().toString() : null,
        a.getApprovedAt() != null ? a.getApprovedAt().toString() : null,
        a.getStatus(),
        a.getApprovalReason(),
        a.getApprovalNotes(),
        a.getDueDate() != null ? a.getDueDate().toString() : null,
        a.getPriority(),
        a.isOverdue(),
        // Traceability keys
        a.getFeatureId(),
        a.getDecisionId(),
        a.getApplicability(),
        a.getConfidentialityLevel());
  }

  private OemCustomizationDto toCustomizationDto(OemCustomization c) {
    return new OemCustomizationDto(
        c.getId().toString(),
        c.getEntityId(),
        c.getEntityType(),
        c.getOemCode(),
        c.getType(),
        c.getCustomParameters(),
        c.getOriginalParameters(),
        c.getCustomizationReason(),
        c.getApprovedBy() != null ? c.getApprovedBy().toString() : null,
        c.getApprovedAt() != null ? c.getApprovedAt().toString() : null,
        c.getStatus(),
        c.getApprovalNotes());
  }
}
