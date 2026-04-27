package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.oemtraceability.OemTraceabilityPermissions;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import org.springframework.stereotype.Component;

@Component
public class OemTraceabilityPermissionDefinitionContributor
    implements PermissionDefinitionContributor {

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(OemTraceabilityPermissions.GROUP, "OEM Traceability");
    group.addPermission(OemTraceabilityPermissions.APPROVAL_DEFAULT);
    group.addPermission(OemTraceabilityPermissions.APPROVAL_CREATE);
    group.addPermission(OemTraceabilityPermissions.APPROVAL_MANAGE);
    group.addPermission(OemTraceabilityPermissions.CUSTOMIZATION_DEFAULT);
    group.addPermission(OemTraceabilityPermissions.CUSTOMIZATION_CREATE);
    group.addPermission(OemTraceabilityPermissions.CUSTOMIZATION_MANAGE);
  }
}
