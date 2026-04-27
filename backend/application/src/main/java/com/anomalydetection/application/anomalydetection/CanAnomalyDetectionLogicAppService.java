package com.anomalydetection.application.anomalydetection;

import com.anomalydetection.contracts.anomalydetection.CanAnomalyDetectionLogicDto;
import com.anomalydetection.contracts.anomalydetection.CreateUpdateCanAnomalyDetectionLogicDto;
import com.anomalydetection.domain.anomalydetection.AnomalyType;
import com.anomalydetection.domain.anomalydetection.CanAnomalyDetectionLogic;
import com.anomalydetection.domain.anomalydetection.CanAnomalyDetectionLogicRepository;
import com.anomalydetection.domain.anomalydetection.DetectionLogicStatus;
import com.anomalydetection.domain.anomalydetection.SharingLevel;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CanAnomalyDetectionLogicAppService {

  private final CanAnomalyDetectionLogicRepository repository;
  private final ICurrentTenant currentTenant;

  public CanAnomalyDetectionLogicAppService(
      CanAnomalyDetectionLogicRepository repository, ICurrentTenant currentTenant) {
    this.repository = repository;
    this.currentTenant = currentTenant;
  }

  @Transactional(readOnly = true)
  public List<CanAnomalyDetectionLogicDto> getList() {
    return repository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public Optional<CanAnomalyDetectionLogicDto> getById(UUID id) {
    return repository.findById(id).map(this::toDto);
  }

  @Transactional(readOnly = true)
  public List<CanAnomalyDetectionLogicDto> getByStatus(DetectionLogicStatus status) {
    return repository.findAllByStatus(status).stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<CanAnomalyDetectionLogicDto> getByAnomalyType(AnomalyType type) {
    return repository.findAllByAnomalyType(type).stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<CanAnomalyDetectionLogicDto> getBySharingLevel(SharingLevel level) {
    return repository.findAllBySharingLevel(level).stream().map(this::toDto).toList();
  }

  public CanAnomalyDetectionLogicDto create(CreateUpdateCanAnomalyDetectionLogicDto input) {
    var entity = new CanAnomalyDetectionLogic(
        UUID.randomUUID(),
        input.name(),
        input.version() != null ? input.version() : "1.0.0");
    applyInput(entity, input);
    currentTenant.getTenantId().ifPresent(entity::setTenantId);
    return toDto(repository.save(entity));
  }

  public Optional<CanAnomalyDetectionLogicDto> update(
      UUID id, CreateUpdateCanAnomalyDetectionLogicDto input) {
    return repository.findById(id)
        .map(entity -> {
          applyInput(entity, input);
          return toDto(repository.save(entity));
        });
  }

  public boolean delete(UUID id) {
    if (!repository.existsById(id)) return false;
    repository.deleteById(id);
    return true;
  }

  public Optional<CanAnomalyDetectionLogicDto> submitForApproval(UUID id) {
    return repository.findById(id)
        .map(entity -> {
          entity.submitForApproval();
          return toDto(repository.save(entity));
        });
  }

  public Optional<CanAnomalyDetectionLogicDto> approve(UUID id, UUID approvedBy, String notes) {
    return repository.findById(id)
        .map(entity -> {
          entity.approve(approvedBy, notes);
          return toDto(repository.save(entity));
        });
  }

  public Optional<CanAnomalyDetectionLogicDto> reject(UUID id, String reason) {
    return repository.findById(id)
        .map(entity -> {
          entity.reject(reason);
          return toDto(repository.save(entity));
        });
  }

  public Optional<CanAnomalyDetectionLogicDto> deprecate(UUID id, String reason) {
    return repository.findById(id)
        .map(entity -> {
          entity.deprecate(reason);
          return toDto(repository.save(entity));
        });
  }

  private void applyInput(CanAnomalyDetectionLogic entity, CreateUpdateCanAnomalyDetectionLogicDto i) {
    entity.setName(i.name());
    if (i.version() != null) entity.setVersion(i.version());
    entity.setOemCode(i.oemCode());
    entity.setAnomalyType(i.anomalyType());
    entity.setDescription(i.description());
    entity.setTargetSystemType(i.targetSystemType());
    if (i.complexity() != null) entity.setComplexity(i.complexity());
    entity.setRequirements(i.requirements());
    if (i.implementationType() != null) entity.setImplementationType(i.implementationType());
    entity.setImplementationContent(i.implementationContent());
    entity.setImplementationLanguage(i.implementationLanguage());
    entity.setImplementationEntryPoint(i.implementationEntryPoint());
    if (i.asilLevel() != null) entity.setAsilLevel(i.asilLevel());
    entity.setSafetyRequirementId(i.safetyRequirementId());
    entity.setSafetyGoalId(i.safetyGoalId());
    entity.setHazardAnalysisId(i.hazardAnalysisId());
    if (i.sharingLevel() != null) entity.setSharingLevel(i.sharingLevel());
    entity.setVehiclePhaseId(i.vehiclePhaseId());
  }

  private CanAnomalyDetectionLogicDto toDto(CanAnomalyDetectionLogic e) {
    return new CanAnomalyDetectionLogicDto(
        e.getId(),
        e.getTenantId(),
        e.getName(),
        e.getVersion(),
        e.getOemCode(),
        e.getAnomalyType(),
        e.getDescription(),
        e.getTargetSystemType(),
        e.getComplexity(),
        e.getRequirements(),
        e.getImplementationType(),
        e.getImplementationLanguage(),
        e.getAsilLevel(),
        e.getSafetyRequirementId(),
        e.getSafetyGoalId(),
        e.getStatus(),
        e.getSharingLevel(),
        e.getVehiclePhaseId(),
        e.getApprovedAt(),
        e.getApprovedBy(),
        e.getApprovalNotes(),
        e.getExecutionCount(),
        e.getLastExecutedAt(),
        e.getLastExecutionTimeMs());
  }
}
