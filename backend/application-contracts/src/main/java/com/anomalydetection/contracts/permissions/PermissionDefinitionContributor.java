package com.anomalydetection.contracts.permissions;

/**
 * Implemented by each feature module to declare the permissions it owns.
 * All contributors are collected by PermissionDefinitionSynchronizer at startup.
 */
public interface PermissionDefinitionContributor {
  void define(PermissionDefinitionContext context);
}