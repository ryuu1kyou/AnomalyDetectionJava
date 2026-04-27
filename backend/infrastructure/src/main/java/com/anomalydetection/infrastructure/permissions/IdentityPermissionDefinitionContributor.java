package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.identity.IdentityPermissions;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import org.springframework.stereotype.Component;

@Component
public class IdentityPermissionDefinitionContributor implements PermissionDefinitionContributor {

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(IdentityPermissions.GROUP, "Identity");
    group.addPermission(IdentityPermissions.USERS_VIEW);
    group.addPermission(IdentityPermissions.USERS_CREATE);
    group.addPermission(IdentityPermissions.USERS_EDIT);
    group.addPermission(IdentityPermissions.USERS_DELETE);
    group.addPermission(IdentityPermissions.ROLES_VIEW);
    group.addPermission(IdentityPermissions.ROLES_CREATE);
    group.addPermission(IdentityPermissions.ROLES_EDIT);
    group.addPermission(IdentityPermissions.ROLES_DELETE);
    group.addPermission(IdentityPermissions.TENANTS_VIEW);
    group.addPermission(IdentityPermissions.TENANTS_CREATE);
    group.addPermission(IdentityPermissions.TENANTS_EDIT);
    group.addPermission(IdentityPermissions.TENANTS_DELETE);
  }
}