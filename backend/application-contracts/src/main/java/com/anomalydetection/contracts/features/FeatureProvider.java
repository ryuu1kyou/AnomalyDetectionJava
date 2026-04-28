package com.anomalydetection.contracts.features;

import java.util.List;

public interface FeatureProvider {
  List<FeatureDto> listAll();
  void setGlobal(String name, boolean enabled);
  void setForTenant(String name, boolean enabled, String tenantId);
}
