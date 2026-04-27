package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import com.anomalydetection.contracts.safety.SafetyTracePermissions;
import org.springframework.stereotype.Component;

@Component
public class SafetyPermissionDefinitionContributor implements PermissionDefinitionContributor {

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(SafetyTracePermissions.GROUP, "Safety Trace (ISO 26262)");
    group.addPermission(SafetyTracePermissions.DEFAULT);
    group.addPermission(SafetyTracePermissions.CREATE);
    group.addPermission(SafetyTracePermissions.EDIT);
    group.addPermission(SafetyTracePermissions.DELETE);
    group.addPermission(SafetyTracePermissions.APPROVE);
    group.addPermission(SafetyTracePermissions.AUDIT_EXPORT);
  }
}
