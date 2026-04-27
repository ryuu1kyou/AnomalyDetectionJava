package com.anomalydetection.application.detectiontemplates;

import com.anomalydetection.contracts.detectiontemplates.CreateUpdateDetectionTemplateDto;
import com.anomalydetection.contracts.detectiontemplates.DetectionTemplateDto;
import com.anomalydetection.contracts.detectiontemplates.DetectionTemplatePermissions;
import com.anomalydetection.domain.detectiontemplates.DetectionTemplate;
import com.anomalydetection.domain.detectiontemplates.DetectionTemplateRepository;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DetectionTemplateAppService {

  private final DetectionTemplateRepository repository;
  private final ICurrentTenant currentTenant;

  public DetectionTemplateAppService(DetectionTemplateRepository repository, ICurrentTenant currentTenant) {
    this.repository = repository;
    this.currentTenant = currentTenant;
  }

  @Transactional(readOnly = true)
  public List<DetectionTemplateDto> getList() {
    return repository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public Optional<DetectionTemplateDto> getById(UUID id) {
    return repository.findById(id).map(this::toDto);
  }

  public DetectionTemplateDto create(CreateUpdateDetectionTemplateDto input) {
    var entity = new DetectionTemplate(UUID.randomUUID(), input.name());
    currentTenant.getTenantId().ifPresent(entity::setTenantId);
    entity.setDescription(input.description());
    entity.setCanSignalId(input.canSignalId());
    entity.setExpression(input.expression());
    entity.setThreshold(input.threshold());
    entity.setActive(input.isActive());
    return toDto(repository.save(entity));
  }

  public Optional<DetectionTemplateDto> update(UUID id, CreateUpdateDetectionTemplateDto input) {
    return repository.findById(id)
        .map(
            entity -> {
              entity.setName(input.name());
              entity.setDescription(input.description());
              entity.setCanSignalId(input.canSignalId());
              entity.setExpression(input.expression());
              entity.setThreshold(input.threshold());
              entity.setActive(input.isActive());
              return toDto(repository.save(entity));
            });
  }

  public boolean delete(UUID id) {
    if (!repository.existsById(id)) return false;
    repository.deleteById(id);
    return true;
  }

  private DetectionTemplateDto toDto(DetectionTemplate t) {
    return new DetectionTemplateDto(
        t.getId(),
        t.getTenantId(),
        t.getName(),
        t.getDescription(),
        t.getCanSignalId(),
        t.getExpression(),
        t.getThreshold(),
        t.isActive());
  }
}
