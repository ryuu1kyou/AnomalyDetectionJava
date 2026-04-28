package com.anomalydetection.application.safety;

import com.anomalydetection.contracts.safety.SafetyTracePermissions;
import com.anomalydetection.contracts.safety.CreateSafetyTraceLinkDto;
import com.anomalydetection.contracts.safety.CreateSafetyTraceRecordDto;
import com.anomalydetection.contracts.safety.GetSafetyTraceInput;
import com.anomalydetection.contracts.safety.SafetyTraceLinkDto;
import com.anomalydetection.contracts.safety.SafetyTraceRecordDto;
import com.anomalydetection.contracts.safety.UpdateSafetyTraceRecordDto;
import com.anomalydetection.domain.safety.SafetyApprovalStatus;
import com.anomalydetection.domain.safety.SafetyTraceLink;
import com.anomalydetection.domain.safety.SafetyTraceLinkRepository;
import com.anomalydetection.domain.safety.SafetyTraceRecord;
import com.anomalydetection.domain.safety.SafetyTraceRecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SafetyTraceAppService {

  private final SafetyTraceRecordRepository recordRepo;
  private final SafetyTraceLinkRepository linkRepo;
  private final ObjectMapper objectMapper;

  public SafetyTraceAppService(
      SafetyTraceRecordRepository recordRepo,
      SafetyTraceLinkRepository linkRepo,
      ObjectMapper objectMapper) {
    this.recordRepo = recordRepo;
    this.linkRepo = linkRepo;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + SafetyTracePermissions.DEFAULT + "')")
  public List<SafetyTraceRecordDto> getList(GetSafetyTraceInput input) {
    var all = recordRepo.findAll().stream()
        .filter(r -> {
          if (input.filter() != null && !input.filter().isBlank()) {
            var hay = (r.getName() + " " + nullStr(r.getRequirementId())).toLowerCase();
            if (!hay.contains(input.filter().toLowerCase())) return false;
          }
          if (input.asilLevel() != null && !input.asilLevel().isBlank()) {
            if (!input.asilLevel().equalsIgnoreCase(r.getAsilLevel())) return false;
          }
          if (input.approvalStatus() != null && !input.approvalStatus().isBlank()) {
            try {
              var s = SafetyApprovalStatus.valueOf(input.approvalStatus());
              if (r.getApprovalStatus() != s) return false;
            } catch (IllegalArgumentException ignored) {}
          }
          if (input.projectId() != null && !input.projectId().isBlank()) {
            if (r.getProjectId() == null
                || !r.getProjectId().toString().equals(input.projectId())) return false;
          }
          if (input.detectionLogicId() != null && !input.detectionLogicId().isBlank()) {
            if (r.getDetectionLogicId() == null
                || !r.getDetectionLogicId().toString().equals(input.detectionLogicId())) return false;
          }
          return true;
        })
        .toList();

    int skip = input.skipCount() != null ? Math.max(0, input.skipCount()) : 0;
    int take = input.maxResultCount() != null && input.maxResultCount() > 0
        ? input.maxResultCount() : 50;

    return all.stream().skip(skip).limit(take).map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + SafetyTracePermissions.DEFAULT + "')")
  public Optional<SafetyTraceRecordDto> getById(UUID id) {
    return recordRepo.findById(id).map(this::toDto);
  }

  @PreAuthorize("hasAuthority('" + SafetyTracePermissions.CREATE + "')")
  public SafetyTraceRecordDto create(CreateSafetyTraceRecordDto input) {
    var record = new SafetyTraceRecord(UUID.randomUUID(), input.name(),
        input.asilLevel() != null ? input.asilLevel() : "QM");
    record.setDescription(input.description());
    record.setRequirementId(input.requirementId());
    record.setSafetyGoalId(input.safetyGoalId());
    record.setHazardAnalysisId(input.hazardAnalysisId());
    if (input.detectionLogicId() != null)
      record.setDetectionLogicId(UUID.fromString(input.detectionLogicId()));
    if (input.projectId() != null)
      record.setProjectId(UUID.fromString(input.projectId()));
    record.setRelatedDocuments(toJson(input.relatedDocuments()));
    return toDto(recordRepo.save(record));
  }

  @PreAuthorize("hasAuthority('" + SafetyTracePermissions.EDIT + "')")
  public Optional<SafetyTraceRecordDto> update(UUID id, UpdateSafetyTraceRecordDto input) {
    return recordRepo.findById(id).map(r -> {
      r.setName(input.name());
      r.setDescription(input.description());
      r.setRequirementId(input.requirementId());
      r.setSafetyGoalId(input.safetyGoalId());
      r.setHazardAnalysisId(input.hazardAnalysisId());
      if (input.asilLevel() != null) r.updateAsilLevel(input.asilLevel());
      if (input.detectionLogicId() != null)
        r.setDetectionLogicId(UUID.fromString(input.detectionLogicId()));
      if (input.projectId() != null)
        r.setProjectId(UUID.fromString(input.projectId()));
      r.setRelatedDocuments(toJson(input.relatedDocuments()));
      return toDto(recordRepo.save(r));
    });
  }

  @PreAuthorize("hasAuthority('" + SafetyTracePermissions.DELETE + "')")
  public boolean delete(UUID id) {
    if (!recordRepo.existsById(id)) return false;
    recordRepo.deleteById(id);
    return true;
  }

  @PreAuthorize("hasAuthority('" + SafetyTracePermissions.EDIT + "')")
  public Optional<SafetyTraceRecordDto> submit(UUID id) {
    return recordRepo.findById(id).map(r -> {
      r.submit(null);
      return toDto(recordRepo.save(r));
    });
  }

  @PreAuthorize("hasAuthority('" + SafetyTracePermissions.APPROVE + "')")
  public Optional<SafetyTraceRecordDto> approve(UUID id, String comments) {
    return recordRepo.findById(id).map(r -> {
      r.approve(null, comments);
      return toDto(recordRepo.save(r));
    });
  }

  @PreAuthorize("hasAuthority('" + SafetyTracePermissions.APPROVE + "')")
  public Optional<SafetyTraceRecordDto> reject(UUID id, String comments) {
    return recordRepo.findById(id).map(r -> {
      r.reject(null, comments);
      return toDto(recordRepo.save(r));
    });
  }

  // --- Links ---

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + SafetyTracePermissions.DEFAULT + "')")
  public List<SafetyTraceLinkDto> getLinks(UUID recordId) {
    var sources = linkRepo.findAllBySourceRecordId(recordId);
    var targets = linkRepo.findAllByTargetRecordId(recordId);
    return java.util.stream.Stream.concat(sources.stream(), targets.stream())
        .distinct()
        .map(this::toLinkDto)
        .toList();
  }

  @PreAuthorize("hasAuthority('" + SafetyTracePermissions.CREATE + "')")
  public SafetyTraceLinkDto createLink(CreateSafetyTraceLinkDto input) {
    var link = new SafetyTraceLink(
        UUID.randomUUID(),
        UUID.fromString(input.sourceRecordId()),
        UUID.fromString(input.targetRecordId()),
        input.linkType(),
        input.relation());
    return toLinkDto(linkRepo.save(link));
  }

  @PreAuthorize("hasAuthority('" + SafetyTracePermissions.DELETE + "')")
  public boolean deleteLink(UUID id) {
    if (!linkRepo.existsById(id)) return false;
    linkRepo.deleteById(id);
    return true;
  }

  // --- Mappers ---

  private SafetyTraceRecordDto toDto(SafetyTraceRecord r) {
    return new SafetyTraceRecordDto(
        r.getId().toString(),
        r.getName(),
        r.getDescription(),
        r.getRequirementId(),
        r.getSafetyGoalId(),
        r.getHazardAnalysisId(),
        r.getAsilLevel(),
        r.getDetectionLogicId() != null ? r.getDetectionLogicId().toString() : null,
        r.getProjectId() != null ? r.getProjectId().toString() : null,
        r.getVersion(),
        r.getApprovalStatus(),
        r.getSubmittedAt() != null ? r.getSubmittedAt().toString() : null,
        r.getApprovedAt() != null ? r.getApprovedAt().toString() : null,
        r.getApprovalComments(),
        fromJson(r.getRelatedDocuments()));
  }

  private SafetyTraceLinkDto toLinkDto(SafetyTraceLink l) {
    return new SafetyTraceLinkDto(
        l.getId().toString(),
        l.getSourceRecordId().toString(),
        l.getTargetRecordId().toString(),
        l.getLinkType(),
        l.getRelation());
  }

  private List<String> fromJson(String json) {
    if (json == null || json.isBlank()) return List.of();
    try {
      return objectMapper.readValue(json, new TypeReference<List<String>>() {});
    } catch (Exception e) {
      return List.of();
    }
  }

  private String toJson(List<String> list) {
    if (list == null || list.isEmpty()) return "[]";
    try {
      return objectMapper.writeValueAsString(list);
    } catch (Exception e) {
      return "[]";
    }
  }

  private static String nullStr(String s) {
    return s != null ? s : "";
  }
}
