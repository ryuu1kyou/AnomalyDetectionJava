package com.anomalydetection.application.permissions;

import com.anomalydetection.contracts.permissions.GrantDto;
import com.anomalydetection.contracts.permissions.PermissionDefinitionRegistry;
import com.anomalydetection.contracts.permissions.PermissionGroupDefinition;
import com.anomalydetection.domain.permissions.PermissionGrant;
import com.anomalydetection.domain.permissions.PermissionGrantRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PermissionManager {

  private final PermissionGrantRepository repository;
  private final PermissionDefinitionRegistry registry;

  public PermissionManager(PermissionGrantRepository repository,
      PermissionDefinitionRegistry registry) {
    this.repository = repository;
    this.registry = registry;
  }

  public void grantToRole(String permissionName, String roleName, UUID tenantId) {
    if (!repository.existsByNameAndProviderNameAndProviderKey(permissionName, "R", roleName)) {
      repository.save(new PermissionGrant(permissionName, "R", roleName, tenantId));
    }
  }

  public void revokeFromRole(String permissionName, String roleName) {
    repository.deleteByNameAndProviderNameAndProviderKey(permissionName, "R", roleName);
  }

  public void grantToUser(String permissionName, String userId, UUID tenantId) {
    if (!repository.existsByNameAndProviderNameAndProviderKey(permissionName, "U", userId)) {
      repository.save(new PermissionGrant(permissionName, "U", userId, tenantId));
    }
  }

  @Transactional(readOnly = true)
  public List<String> getPermissionsForRoles(List<String> roleNames) {
    return roleNames.stream()
        .flatMap(role -> repository.findByProviderNameAndProviderKey("R", role).stream())
        .map(PermissionGrant::getName)
        .distinct()
        .toList();
  }

  @Transactional(readOnly = true)
  public List<PermissionGroupDefinition> getDefinitions() {
    return registry.getGroups();
  }

  @Transactional(readOnly = true)
  public List<GrantDto> getGrants() {
    return repository.findAll().stream()
        .map(g -> new GrantDto(g.getName(), g.getProviderName(), g.getProviderKey()))
        .toList();
  }
}
