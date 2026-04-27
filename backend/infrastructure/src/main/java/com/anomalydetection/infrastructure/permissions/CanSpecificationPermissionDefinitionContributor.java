package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.cansspecification.CanSpecificationPermissions;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import org.springframework.stereotype.Component;

@Component
public class CanSpecificationPermissionDefinitionContributor implements PermissionDefinitionContributor {

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(CanSpecificationPermissions.GROUP, "Can Specification");
    group.addPermission(CanSpecificationPermissions.DEFAULT);
    group.addPermission(CanSpecificationPermissions.CREATE);
    group.addPermission(CanSpecificationPermissions.EDIT);
    group.addPermission(CanSpecificationPermissions.DELETE);
  }
}