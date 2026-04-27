package com.anomalydetection.application.permissions;

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

  public PermissionManager(PermissionGrantRepository repository) {
    this.repository = repository;
  }

  public void grantToRole(String permissionName, String roleName, UUID tenantId) {
    if (!repository.existsByNameAndProviderNameAndProviderKey(permissionName, "R", roleName)) {
      repository.save(new PermissionGrant(permissionName, "R", roleName, tenantId));
    }
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
}