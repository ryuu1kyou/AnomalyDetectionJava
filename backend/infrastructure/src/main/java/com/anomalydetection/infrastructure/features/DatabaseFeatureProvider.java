package com.anomalydetection.infrastructure.features;

import com.anomalydetection.contracts.features.FeatureDto;
import com.anomalydetection.contracts.features.FeatureProvider;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DatabaseFeatureProvider implements FeatureProvider {

  private final JpaFeatureValueRepository repository;

  public DatabaseFeatureProvider(JpaFeatureValueRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<FeatureDto> listAll() {
    return repository.findAll().stream()
        .map(e -> new FeatureDto(
            e.getName(),
            Boolean.parseBoolean(e.getValue()),
            e.getProviderName(),
            e.getProviderKey()))
        .toList();
  }

  @Override
  @CacheEvict(value = "features", key = "'G:' + #name")
  public void setGlobal(String name, boolean enabled) {
    upsert(name, String.valueOf(enabled), "G", null);
  }

  @Override
  @CacheEvict(value = "features", key = "'T:' + #tenantId + ':' + #name")
  public void setForTenant(String name, boolean enabled, String tenantId) {
    upsert(name, String.valueOf(enabled), "T", tenantId);
  }

  private void upsert(String name, String value, String providerName, String providerKey) {
    FeatureValueEntity entity = repository
        .findByNameAndProviderNameAndProviderKey(name, providerName, providerKey)
        .orElse(null);
    if (entity != null) {
      entity.setValue(value);
    } else {
      entity = new FeatureValueEntity(name, value, providerName, providerKey);
    }
    repository.save(entity);
  }
}
