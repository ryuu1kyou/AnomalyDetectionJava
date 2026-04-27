package com.anomalydetection.application.cansignals;

import com.anomalydetection.contracts.cansignals.CanSignalDto;
import com.anomalydetection.contracts.cansignals.CanSignalPermissions;
import com.anomalydetection.contracts.cansignals.CreateUpdateCanSignalDto;
import com.anomalydetection.domain.cansignals.CanSignal;
import com.anomalydetection.domain.cansignals.CanSignalRepository;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CanSignalAppService {

  private final CanSignalRepository repository;
  private final ICurrentTenant currentTenant;

  public CanSignalAppService(CanSignalRepository repository, ICurrentTenant currentTenant) {
    this.repository = repository;
    this.currentTenant = currentTenant;
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + CanSignalPermissions.DEFAULT + "')")
  public List<CanSignalDto> getList() {
    return repository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + CanSignalPermissions.DEFAULT + "')")
  public Optional<CanSignalDto> getById(UUID id) {
    return repository.findById(id).map(this::toDto);
  }

  @PreAuthorize("hasAuthority('" + CanSignalPermissions.CREATE + "')")
  public CanSignalDto create(CreateUpdateCanSignalDto input) {
    var entity = new CanSignal(UUID.randomUUID(), input.frameId(), input.name(), input.startBit(), input.length());
    currentTenant.getTenantId().ifPresent(entity::setTenantId);
    entity.setDescription(input.description());
    entity.setByteOrder(input.byteOrder());
    entity.setSigned(input.isSigned());
    entity.setSpecificationId(input.specificationId());
    return toDto(repository.save(entity));
  }

  @PreAuthorize("hasAuthority('" + CanSignalPermissions.EDIT + "')")
  public Optional<CanSignalDto> update(UUID id, CreateUpdateCanSignalDto input) {
    return repository.findById(id).map(entity -> {
      entity.setFrameId(input.frameId());
      entity.setName(input.name());
      entity.setDescription(input.description());
      entity.setStartBit(input.startBit());
      entity.setLength(input.length());
      entity.setByteOrder(input.byteOrder());
      entity.setSigned(input.isSigned());
      entity.setSpecificationId(input.specificationId());
      return toDto(repository.save(entity));
    });
  }

  @PreAuthorize("hasAuthority('" + CanSignalPermissions.DELETE + "')")
  public boolean delete(UUID id) {
    if (!repository.existsById(id)) return false;
    repository.deleteById(id);
    return true;
  }

  private CanSignalDto toDto(CanSignal s) {
    return new CanSignalDto(s.getId(), s.getTenantId(), s.getFrameId(), s.getName(),
        s.getDescription(), s.getStartBit(), s.getLength(), s.getByteOrder(),
        s.isSigned(), s.getSpecificationId());
  }
}