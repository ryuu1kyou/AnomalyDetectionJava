package com.anomalydetection.application.anomalydetection;

import com.anomalydetection.contracts.anomalydetection.CanAnomalyDetectionLogicDto;
import com.anomalydetection.contracts.anomalydetection.CreateUpdateCanAnomalyDetectionLogicDto;
import com.anomalydetection.domain.anomalydetection.AnomalyType;
import com.anomalydetection.domain.anomalydetection.CanAnomalyDetectionLogic;
import com.anomalydetection.domain.anomalydetection.CanAnomalyDetectionLogicRepository;
import com.anomalydetection.domain.anomalydetection.DetectionLogicStatus;
import com.anomalydetection.contracts.anomalydetection.AnomalyDetectionPermissions;
import com.anomalydetection.domain.anomalydetection.SharingLevel;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CanAnomalyDetectionLogicAppService {

  private final CanAnomalyDetectionLogicRepository repository;
  private final ICurrentTenant currentTenant;
  private final CanAnomalyDetectionLogicMapper mapper;

  public CanAnomalyDetectionLogicAppService(
      CanAnomalyDetectionLogicRepository repository, ICurrentTenant currentTenant,
      CanAnomalyDetectionLogicMapper mapper) {
    this.repository = repository;
    this.currentTenant = currentTenant;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_DEFAULT + "')")
  public List<CanAnomalyDetectionLogicDto> getList() {
    return repository.findAll().stream().map(mapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_DEFAULT + "')")
  public Optional<CanAnomalyDetectionLogicDto> getById(UUID id) {
    return repository.findById(id).map(mapper::toDto);
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_DEFAULT + "')")
  public List<CanAnomalyDetectionLogicDto> getByStatus(DetectionLogicStatus status) {
    return repository.findAllByStatus(status).stream().map(mapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_DEFAULT + "')")
  public List<CanAnomalyDetectionLogicDto> getByAnomalyType(AnomalyType type) {
    return repository.findAllByAnomalyType(type).stream().map(mapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_DEFAULT + "')")
  public List<CanAnomalyDetectionLogicDto> getBySharingLevel(SharingLevel level) {
    return repository.findAllBySharingLevel(level).stream().map(mapper::toDto).toList();
  }

  @PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_CREATE + "')")
  public CanAnomalyDetectionLogicDto create(CreateUpdateCanAnomalyDetectionLogicDto input) {
    var entity = new CanAnomalyDetectionLogic(
        UUID.randomUUID(),
        input.name(),
        input.version() != null ? input.version() : "1.0.0");
    applyInput(entity, input);
    currentTenant.getTenantId().ifPresent(entity::setTenantId);
    return mapper.toDto(repository.save(entity));
  }

  @PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_EDIT + "')")
  public Optional<CanAnomalyDetectionLogicDto> update(
      UUID id, CreateUpdateCanAnomalyDetectionLogicDto input) {
    return repository.findById(id)
        .map(entity -> {
          applyInput(entity, input);
          return mapper.toDto(repository.save(entity));
        });
  }

  @PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_DELETE + "')")
  public boolean delete(UUID id) {
    if (!repository.existsById(id)) return false;
    repository.deleteById(id);
    return true;
  }

  @PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_EDIT + "')")
  public Optional<CanAnomalyDetectionLogicDto> submitForApproval(UUID id) {
    return repository.findById(id)
        .map(entity -> {
          entity.submitForApproval();
          return mapper.toDto(repository.save(entity));
        });
  }

  @PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_APPROVE + "')")
  public Optional<CanAnomalyDetectionLogicDto> approve(UUID id, UUID approvedBy, String notes) {
    return repository.findById(id)
        .map(entity -> {
          entity.approve(approvedBy, notes);
          return mapper.toDto(repository.save(entity));
        });
  }

  @PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_APPROVE + "')")
  public Optional<CanAnomalyDetectionLogicDto> reject(UUID id, String reason) {
    return repository.findById(id)
        .map(entity -> {
          entity.reject(reason);
          return mapper.toDto(repository.save(entity));
        });
  }

  @PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_APPROVE + "')")
  public Optional<CanAnomalyDetectionLogicDto> deprecate(UUID id, String reason) {
    return repository.findById(id)
        .map(entity -> {
          entity.deprecate(reason);
          return mapper.toDto(repository.save(entity));
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

}
