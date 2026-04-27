package com.anomalydetection.application.cansspecification;

import com.anomalydetection.contracts.cansspecification.CanSpecificationPermissions;
import com.anomalydetection.contracts.cansspecification.CanSystemCategoryDto;
import com.anomalydetection.contracts.cansspecification.CreateUpdateCanSystemCategoryDto;
import com.anomalydetection.domain.cansspecification.CanSystemCategory;
import com.anomalydetection.domain.cansspecification.CanSystemCategoryRepository;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CanSystemCategoryAppService {

  private final CanSystemCategoryRepository repository;
  private final ICurrentTenant currentTenant;

  public CanSystemCategoryAppService(CanSystemCategoryRepository repository, ICurrentTenant currentTenant) {
    this.repository = repository;
    this.currentTenant = currentTenant;
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + CanSpecificationPermissions.DEFAULT + "')")
  public List<CanSystemCategoryDto> getList() {
    return repository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + CanSpecificationPermissions.DEFAULT + "')")
  public Optional<CanSystemCategoryDto> getById(UUID id) {
    return repository.findById(id).map(this::toDto);
  }

  @PreAuthorize("hasAuthority('" + CanSpecificationPermissions.CREATE + "')")
  public CanSystemCategoryDto create(CreateUpdateCanSystemCategoryDto input) {
    var entity = new CanSystemCategory(UUID.randomUUID(), input.name());
    currentTenant.getTenantId().ifPresent(entity::setTenantId);
    entity.setDescription(input.description());
    entity.setDisplayOrder(input.displayOrder());
    return toDto(repository.save(entity));
  }

  @PreAuthorize("hasAuthority('" + CanSpecificationPermissions.EDIT + "')")
  public Optional<CanSystemCategoryDto> update(UUID id, CreateUpdateCanSystemCategoryDto input) {
    return repository.findById(id).map(entity -> {
      entity.setName(input.name());
      entity.setDescription(input.description());
      entity.setDisplayOrder(input.displayOrder());
      return toDto(repository.save(entity));
    });
  }

  @PreAuthorize("hasAuthority('" + CanSpecificationPermissions.DELETE + "')")
  public boolean delete(UUID id) {
    if (!repository.existsById(id)) return false;
    repository.deleteById(id);
    return true;
  }

  private CanSystemCategoryDto toDto(CanSystemCategory c) {
    return new CanSystemCategoryDto(c.getId(), c.getTenantId(), c.getName(), c.getDescription(), c.getDisplayOrder());
  }
}