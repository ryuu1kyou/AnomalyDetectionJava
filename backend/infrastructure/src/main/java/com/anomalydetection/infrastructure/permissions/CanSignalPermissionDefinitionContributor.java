package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.cansignals.CanSignalPermissions;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import org.springframework.stereotype.Component;

@Component
public class CanSignalPermissionDefinitionContributor implements PermissionDefinitionContributor {

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(CanSignalPermissions.GROUP, "Can Signal");
    group.addPermission(CanSignalPermissions.DEFAULT);
    group.addPermission(CanSignalPermissions.CREATE);
    group.addPermission(CanSignalPermissions.EDIT);
    group.addPermission(CanSignalPermissions.DELETE);
  }
}