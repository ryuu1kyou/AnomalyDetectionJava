package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.anomalydetection.AnomalyDetectionPermissions;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import org.springframework.stereotype.Component;

@Component
public class AnomalyDetectionPermissionDefinitionContributor
    implements PermissionDefinitionContributor {

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(AnomalyDetectionPermissions.GROUP, "Anomaly Detection");
    group.addPermission(AnomalyDetectionPermissions.LOGIC_DEFAULT);
    group.addPermission(AnomalyDetectionPermissions.LOGIC_CREATE);
    group.addPermission(AnomalyDetectionPermissions.LOGIC_EDIT);
    group.addPermission(AnomalyDetectionPermissions.LOGIC_DELETE);
    group.addPermission(AnomalyDetectionPermissions.LOGIC_APPROVE);
    group.addPermission(AnomalyDetectionPermissions.RESULT_DEFAULT);
    group.addPermission(AnomalyDetectionPermissions.RESULT_CREATE);
    group.addPermission(AnomalyDetectionPermissions.RESULT_EDIT);
    group.addPermission(AnomalyDetectionPermissions.RESULT_DELETE);
  }
}
