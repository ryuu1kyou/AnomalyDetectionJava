package com.anomalydetection.application.anomalydetection;

import com.anomalydetection.contracts.anomalydetection.AnomalyDetectionResultDto;
import com.anomalydetection.contracts.anomalydetection.CreateAnomalyDetectionResultDto;
import com.anomalydetection.domain.anomalydetection.AnomalyDetectionResult;
import com.anomalydetection.domain.anomalydetection.AnomalyDetectionResultRepository;
import com.anomalydetection.domain.anomalydetection.AnomalyLevel;
import com.anomalydetection.domain.anomalydetection.ResolutionStatus;
import com.anomalydetection.domain.anomalydetection.SharingLevel;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnomalyDetectionResultAppService {

  private final AnomalyDetectionResultRepository repository;
  private final ICurrentTenant currentTenant;

  public AnomalyDetectionResultAppService(
      AnomalyDetectionResultRepository repository, ICurrentTenant currentTenant) {
    this.repository = repository;
    this.currentTenant = currentTenant;
  }

  @Transactional(readOnly = true)
  public List<AnomalyDetectionResultDto> getList() {
    return repository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public Optional<AnomalyDetectionResultDto> getById(UUID id) {
    return repository.findById(id).map(this::toDto);
  }

  @Transactional(readOnly = true)
  public List<AnomalyDetectionResultDto> getByDetectionLogicId(UUID logicId) {
    return repository.findAllByDetectionLogicId(logicId).stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<AnomalyDetectionResultDto> getByCanSignalId(UUID canSignalId) {
    return repository.findAllByCanSignalId(canSignalId).stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<AnomalyDetectionResultDto> getUnresolved() {
    return repository.findAllByResolutionStatus(ResolutionStatus.OPEN).stream()
        .map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<AnomalyDetectionResultDto> getByAnomalyLevel(AnomalyLevel level) {
    return repository.findAllByAnomalyLevel(level).stream().map(this::toDto).toList();
  }

  public AnomalyDetectionResultDto create(CreateAnomalyDetectionResultDto input) {
    var entity = new AnomalyDetectionResult(
        UUID.randomUUID(),
        currentTenant.getTenantId().orElse(null),
        input.detectionLogicId(),
        input.canSignalId(),
        input.anomalyLevel(),
        input.anomalyType(),
        input.confidenceScore(),
        input.description());
    entity.setSignalValue(input.signalValue());
    entity.setInputTimestamp(input.inputTimestamp());
    entity.setDetectionType(input.detectionType());
    entity.setTriggerCondition(input.triggerCondition());
    entity.setExecutionTimeMs(input.executionTimeMs());
    entity.setDetectionCondition(input.detectionCondition());
    entity.setDetectionDurationMs(input.detectionDurationMs());
    return toDto(repository.save(entity));
  }

  public boolean delete(UUID id) {
    if (!repository.existsById(id)) return false;
    repository.deleteById(id);
    return true;
  }

  public Optional<AnomalyDetectionResultDto> resolve(UUID id, UUID resolvedBy, String notes) {
    return repository.findById(id)
        .map(entity -> {
          entity.resolve(resolvedBy, notes);
          return toDto(repository.save(entity));
        });
  }

  public Optional<AnomalyDetectionResultDto> markAsFalsePositive(
      UUID id, UUID resolvedBy, String reason) {
    return repository.findById(id)
        .map(entity -> {
          entity.markAsFalsePositive(resolvedBy, reason);
          return toDto(repository.save(entity));
        });
  }

  public Optional<AnomalyDetectionResultDto> share(UUID id, SharingLevel level, UUID sharedBy) {
    return repository.findById(id)
        .map(entity -> {
          entity.share(level, sharedBy);
          return toDto(repository.save(entity));
        });
  }

  private AnomalyDetectionResultDto toDto(AnomalyDetectionResult e) {
    return new AnomalyDetectionResultDto(
        e.getId(),
        e.getTenantId(),
        e.getDetectionLogicId(),
        e.getCanSignalId(),
        e.getDetectedAt(),
        e.getAnomalyLevel(),
        e.getAnomalyType(),
        e.getConfidenceScore(),
        e.getDescription(),
        e.getSignalValue(),
        e.getInputTimestamp(),
        e.getDetectionType(),
        e.getTriggerCondition(),
        e.getExecutionTimeMs(),
        e.isValidated(),
        e.isFalsePositive(),
        e.getDetectionCondition(),
        e.getDetectionDurationMs(),
        e.getResolutionStatus(),
        e.getResolvedAt(),
        e.getResolvedBy(),
        e.getResolutionNotes(),
        e.getSharingLevel(),
        e.isShared());
  }
}
