package com.anomalydetection.application.cansspecification;

import com.anomalydetection.contracts.cansspecification.CanSignalSpecificationDto;
import com.anomalydetection.contracts.cansspecification.CanSpecificationPermissions;
import com.anomalydetection.contracts.cansspecification.CreateUpdateCanSignalSpecificationDto;
import com.anomalydetection.domain.cansspecification.CanSignalSpecification;
import com.anomalydetection.domain.cansspecification.CanSignalSpecification.ConversionType;
import com.anomalydetection.domain.cansspecification.CanSignalSpecificationRepository;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CanSignalSpecificationAppService {

  private final CanSignalSpecificationRepository repository;
  private final ICurrentTenant currentTenant;

  public CanSignalSpecificationAppService(CanSignalSpecificationRepository repository, ICurrentTenant currentTenant) {
    this.repository = repository;
    this.currentTenant = currentTenant;
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + CanSpecificationPermissions.DEFAULT + "')")
  public List<CanSignalSpecificationDto> getList() {
    return repository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + CanSpecificationPermissions.DEFAULT + "')")
  public Optional<CanSignalSpecificationDto> getById(UUID id) {
    return repository.findById(id).map(this::toDto);
  }

  @PreAuthorize("hasAuthority('" + CanSpecificationPermissions.CREATE + "')")
  public CanSignalSpecificationDto create(CreateUpdateCanSignalSpecificationDto input) {
    var entity = new CanSignalSpecification(UUID.randomUUID(), input.signalIdentifier(), input.name(),
        ConversionType.valueOf(input.conversionType()));
    currentTenant.getTenantId().ifPresent(entity::setTenantId);
    entity.setSystemCategoryId(input.systemCategoryId());
    entity.setOffset(input.offset());
    entity.setGain(input.gain());
    entity.setMinValue(input.minValue());
    entity.setMaxValue(input.maxValue());
    entity.setUnit(input.unit());
    entity.setDescription(input.description());
    return toDto(repository.save(entity));
  }

  @PreAuthorize("hasAuthority('" + CanSpecificationPermissions.EDIT + "')")
  public Optional<CanSignalSpecificationDto> update(UUID id, CreateUpdateCanSignalSpecificationDto input) {
    return repository.findById(id).map(entity -> {
      entity.setSignalIdentifier(input.signalIdentifier());
      entity.setName(input.name());
      entity.setSystemCategoryId(input.systemCategoryId());
      entity.setConversionType(ConversionType.valueOf(input.conversionType()));
      entity.setOffset(input.offset());
      entity.setGain(input.gain());
      entity.setMinValue(input.minValue());
      entity.setMaxValue(input.maxValue());
      entity.setUnit(input.unit());
      entity.setDescription(input.description());
      return toDto(repository.save(entity));
    });
  }

  @PreAuthorize("hasAuthority('" + CanSpecificationPermissions.DELETE + "')")
  public boolean delete(UUID id) {
    if (!repository.existsById(id)) return false;
    repository.deleteById(id);
    return true;
  }

  private CanSignalSpecificationDto toDto(CanSignalSpecification s) {
    return new CanSignalSpecificationDto(s.getId(), s.getTenantId(), s.getSignalIdentifier(), s.getName(),
        s.getSystemCategoryId(), s.getConversionType().name(), s.getOffset(), s.getGain(),
        s.getMinValue(), s.getMaxValue(), s.getUnit(), s.getDescription());
  }
}