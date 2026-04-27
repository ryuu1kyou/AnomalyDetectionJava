package com.anomalydetection.contracts.permissions;

import java.util.ArrayList;
import java.util.List;

public class PermissionDefinitionContext {
  private final List<PermissionGroupDefinition> groups = new ArrayList<>();

  public PermissionGroupDefinition addGroup(String name, String displayName) {
    var group = new PermissionGroupDefinition(name, displayName);
    groups.add(group);
    return group;
  }

  public List<String> getAllPermissionNames() {
    return groups.stream()
        .flatMap(g -> g.getPermissionNames().stream())
        .toList();
  }

  public List<PermissionGroupDefinition> getGroups() {
    return List.copyOf(groups);
  }
}