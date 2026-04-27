package com.anomalydetection.infrastructure.features;

import com.anomalydetection.contracts.features.FeatureChecker;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DatabaseFeatureChecker implements FeatureChecker {

  private final JpaFeatureValueRepository repository;

  public DatabaseFeatureChecker(JpaFeatureValueRepository repository) {
    this.repository = repository;
  }

  @Override
  @Cacheable(value = "features", key = "'G:' + #featureName")
  public boolean isEnabled(String featureName) {
    return repository.findByNameAndProviderNameAndProviderKey(featureName, "G", null)
        .map(f -> Boolean.parseBoolean(f.getValue()))
        .orElse(false);
  }

  @Override
  @Cacheable(value = "features", key = "'T:' + #tenantId + ':' + #featureName")
  public boolean isEnabledForTenant(String featureName, String tenantId) {
    Optional<FeatureValueEntity> tenantValue = repository
        .findByNameAndProviderNameAndProviderKey(featureName, "T", tenantId);
    if (tenantValue.isPresent()) {
      return Boolean.parseBoolean(tenantValue.get().getValue());
    }
    return isEnabled(featureName);
  }
}