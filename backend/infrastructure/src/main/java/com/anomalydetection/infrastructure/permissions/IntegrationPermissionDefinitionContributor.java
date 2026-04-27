package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.integration.IntegrationPermissions;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import org.springframework.stereotype.Component;

@Component
public class IntegrationPermissionDefinitionContributor
    implements PermissionDefinitionContributor {

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(IntegrationPermissions.GROUP, "Integration");
    group.addPermission(IntegrationPermissions.DEFAULT);
    group.addPermission(IntegrationPermissions.CREATE);
    group.addPermission(IntegrationPermissions.MANAGE);
    group.addPermission(IntegrationPermissions.IMPORT_DATA);
  }
}
