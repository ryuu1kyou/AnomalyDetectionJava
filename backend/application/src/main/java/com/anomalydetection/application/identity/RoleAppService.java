package com.anomalydetection.application.identity;

import com.anomalydetection.contracts.identity.IdentityPermissions;
import com.anomalydetection.contracts.identity.RoleDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import com.anomalydetection.domain.identity.Role;
import com.anomalydetection.domain.identity.RoleRepository;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleAppService {

  private final RoleRepository roleRepository;
  private final ICurrentTenant currentTenant;

  public RoleAppService(RoleRepository roleRepository, ICurrentTenant currentTenant) {
    this.roleRepository = roleRepository;
    this.currentTenant = currentTenant;
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + IdentityPermissions.ROLES_VIEW + "')")
  public PagedResultDto<RoleDto> getList() {
    List<RoleDto> roles = roleRepository.findAll().stream().map(this::toDto).toList();
    return PagedResultDto.of(roles, roles.size());
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + IdentityPermissions.ROLES_VIEW + "')")
  public Optional<RoleDto> getById(UUID id) {
    return roleRepository.findById(id).map(this::toDto);
  }

  @PreAuthorize("hasAuthority('" + IdentityPermissions.ROLES_CREATE + "')")
  public RoleDto create(String name) {
    var role = new Role(UUID.randomUUID(), name, name.toUpperCase());
    currentTenant.getTenantId().ifPresent(role::setTenantId);
    return toDto(roleRepository.save(role));
  }

  @PreAuthorize("hasAuthority('" + IdentityPermissions.ROLES_DELETE + "')")
  public boolean delete(UUID id) {
    if (!roleRepository.existsById(id)) return false;
    roleRepository.deleteById(id);
    return true;
  }

  private RoleDto toDto(Role r) {
    return new RoleDto(r.getId(), r.getTenantId(), r.getName(), r.isStatic(), r.isDefault());
  }
}
