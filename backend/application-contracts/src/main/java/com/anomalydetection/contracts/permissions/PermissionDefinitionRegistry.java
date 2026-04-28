package com.anomalydetection.contracts.permissions;

import java.util.List;

public interface PermissionDefinitionRegistry {
  List<PermissionGroupDefinition> getGroups();
  List<String> getAllPermissionNames();
}
