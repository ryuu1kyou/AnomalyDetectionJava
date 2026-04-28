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
  private final CanSignalMapper mapper;

  public CanSignalAppService(CanSignalRepository repository, ICurrentTenant currentTenant,
      CanSignalMapper mapper) {
    this.repository = repository;
    this.currentTenant = currentTenant;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + CanSignalPermissions.DEFAULT + "')")
  public List<CanSignalDto> getList() {
    return repository.findAll().stream().map(mapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + CanSignalPermissions.DEFAULT + "')")
  public Optional<CanSignalDto> getById(UUID id) {
    return repository.findById(id).map(mapper::toDto);
  }

  @PreAuthorize("hasAuthority('" + CanSignalPermissions.CREATE + "')")
  public CanSignalDto create(CreateUpdateCanSignalDto input) {
    var entity = new CanSignal(UUID.randomUUID(), input.frameId(), input.name(), input.startBit(), input.length());
    currentTenant.getTenantId().ifPresent(entity::setTenantId);
    entity.setDescription(input.description());
    entity.setByteOrder(input.byteOrder());
    entity.setSigned(input.isSigned());
    entity.setSpecificationId(input.specificationId());
    return mapper.toDto(repository.save(entity));
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
      return mapper.toDto(repository.save(entity));
    });
  }

  @PreAuthorize("hasAuthority('" + CanSignalPermissions.DELETE + "')")
  public boolean delete(UUID id) {
    if (!repository.existsById(id)) return false;
    repository.deleteById(id);
    return true;
  }

}