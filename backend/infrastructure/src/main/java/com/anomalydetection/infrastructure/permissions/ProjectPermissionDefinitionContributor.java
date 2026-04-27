package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import com.anomalydetection.contracts.projects.ProjectPermissions;
import org.springframework.stereotype.Component;

@Component
public class ProjectPermissionDefinitionContributor implements PermissionDefinitionContributor {

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(ProjectPermissions.GROUP, "Projects");
    group.addPermission(ProjectPermissions.DEFAULT);
    group.addPermission(ProjectPermissions.CREATE);
    group.addPermission(ProjectPermissions.EDIT);
    group.addPermission(ProjectPermissions.DELETE);
    group.addPermission(ProjectPermissions.MANAGE_MEMBERS);
    group.addPermission(ProjectPermissions.MANAGE_MILESTONES);
  }
}
