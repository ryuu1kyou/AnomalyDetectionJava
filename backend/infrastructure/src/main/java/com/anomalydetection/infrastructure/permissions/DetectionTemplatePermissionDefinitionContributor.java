package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.detectiontemplates.DetectionTemplatePermissions;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import org.springframework.stereotype.Component;

@Component
public class DetectionTemplatePermissionDefinitionContributor implements PermissionDefinitionContributor {

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(DetectionTemplatePermissions.GROUP, "Detection Templates");
    group.addPermission(DetectionTemplatePermissions.DEFAULT);
    group.addPermission(DetectionTemplatePermissions.CREATE);
    group.addPermission(DetectionTemplatePermissions.EDIT);
    group.addPermission(DetectionTemplatePermissions.DELETE);
  }
}
