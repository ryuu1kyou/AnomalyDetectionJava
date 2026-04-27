package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import com.anomalydetection.contracts.similarpatternsearch.SimilarPatternSearchPermissions;
import org.springframework.stereotype.Component;

@Component
public class SimilarPatternSearchPermissionDefinitionContributor
    implements PermissionDefinitionContributor {

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(SimilarPatternSearchPermissions.GROUP, "Similar Pattern Search");
    group.addPermission(SimilarPatternSearchPermissions.DEFAULT);
    group.addPermission(SimilarPatternSearchPermissions.SEARCH_SIGNALS);
    group.addPermission(SimilarPatternSearchPermissions.COMPARE_TEST_DATA);
  }
}
