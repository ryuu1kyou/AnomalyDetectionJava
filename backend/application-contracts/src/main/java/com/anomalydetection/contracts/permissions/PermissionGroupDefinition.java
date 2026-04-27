package com.anomalydetection.contracts.permissions;

import java.util.ArrayList;
import java.util.List;

public class PermissionGroupDefinition {
  private final String name;
  private final String displayName;
  private final List<String> permissionNames = new ArrayList<>();

  public PermissionGroupDefinition(String name, String displayName) {
    this.name = name;
    this.displayName = displayName;
  }

  public PermissionGroupDefinition addPermission(String permissionName) {
    permissionNames.add(permissionName);
    return this;
  }

  public String getName() { return name; }
  public String getDisplayName() { return displayName; }
  public List<String> getPermissionNames() { return List.copyOf(permissionNames); }
}