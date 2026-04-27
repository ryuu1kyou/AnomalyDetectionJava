package com.anomalydetection.contracts.features;

public interface FeatureChecker {
  boolean isEnabled(String featureName);
  boolean isEnabledForTenant(String featureName, String tenantId);
}