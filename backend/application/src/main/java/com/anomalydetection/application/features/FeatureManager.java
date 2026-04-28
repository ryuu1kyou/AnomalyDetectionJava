package com.anomalydetection.application.features;

import com.anomalydetection.contracts.features.FeatureDto;
import com.anomalydetection.contracts.features.FeatureProvider;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FeatureManager {

  private final FeatureProvider featureProvider;

  public FeatureManager(FeatureProvider featureProvider) {
    this.featureProvider = featureProvider;
  }

  @Transactional(readOnly = true)
  public List<FeatureDto> listAll() {
    return featureProvider.listAll();
  }

  public void setGlobal(String name, boolean enabled) {
    featureProvider.setGlobal(name, enabled);
  }
}
