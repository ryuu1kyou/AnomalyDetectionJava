package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.knowledgebase.KnowledgeBasePermissions;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeBasePermissionDefinitionContributor
    implements PermissionDefinitionContributor {

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(KnowledgeBasePermissions.GROUP, "Knowledge Base");
    group.addPermission(KnowledgeBasePermissions.DEFAULT);
    group.addPermission(KnowledgeBasePermissions.CREATE);
    group.addPermission(KnowledgeBasePermissions.EDIT);
    group.addPermission(KnowledgeBasePermissions.DELETE);
    group.addPermission(KnowledgeBasePermissions.PUBLISH);
  }
}
