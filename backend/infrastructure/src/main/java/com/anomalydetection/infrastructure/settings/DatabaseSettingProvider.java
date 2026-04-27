package com.anomalydetection.infrastructure.settings;

import com.anomalydetection.contracts.settings.SettingProvider;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DatabaseSettingProvider implements SettingProvider {

  private final JpaSettingValueRepository repository;

  public DatabaseSettingProvider(JpaSettingValueRepository repository) {
    this.repository = repository;
  }

  @Override
  @Cacheable(value = "settings", key = "'G:' + #name")
  @Transactional(readOnly = true)
  public Optional<String> getGlobal(String name) {
    return repository.findByNameAndProviderNameAndProviderKey(name, "G", null)
        .map(SettingValueEntity::getValue);
  }

  @Override
  @Cacheable(value = "settings", key = "'T:' + #tenantId + ':' + #name")
  @Transactional(readOnly = true)
  public Optional<String> getForTenant(String name, String tenantId) {
    return repository.findByNameAndProviderNameAndProviderKey(name, "T", tenantId)
        .map(SettingValueEntity::getValue);
  }

  @Override
  @Cacheable(value = "settings", key = "'U:' + #userId + ':' + #name")
  @Transactional(readOnly = true)
  public Optional<String> getForUser(String name, String userId) {
    return repository.findByNameAndProviderNameAndProviderKey(name, "U", userId)
        .map(SettingValueEntity::getValue);
  }

  @Override
  @CacheEvict(value = "settings", key = "'G:' + #name")
  public void setGlobal(String name, String value) {
    upsert(name, value, "G", null);
  }

  @Override
  @CacheEvict(value = "settings", key = "'T:' + #tenantId + ':' + #name")
  public void setForTenant(String name, String value, String tenantId) {
    upsert(name, value, "T", tenantId);
  }

  @Override
  @CacheEvict(value = "settings", key = "#name")
  public void evictCache(String name, String providerName, String providerKey) {
  }

  private void upsert(String name, String value, String providerName, String providerKey) {
    var existing = repository.findByNameAndProviderNameAndProviderKey(name, providerName, providerKey);
    if (existing.isPresent()) {
      existing.get().setValue(value);
      repository.save(existing.get());
    } else {
      repository.save(new SettingValueEntity(name, value, providerName, providerKey));
    }
  }
}