package com.anomalydetection.contracts.settings;

import java.util.Optional;

public interface SettingProvider {
  Optional<String> getGlobal(String name);
  Optional<String> getForTenant(String name, String tenantId);
  Optional<String> getForUser(String name, String userId);
  void setGlobal(String name, String value);
  void setForTenant(String name, String value, String tenantId);
  void evictCache(String name, String providerName, String providerKey);
}