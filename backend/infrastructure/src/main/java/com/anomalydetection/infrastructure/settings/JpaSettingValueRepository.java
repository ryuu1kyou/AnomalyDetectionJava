package com.anomalydetection.infrastructure.settings;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaSettingValueRepository extends JpaRepository<SettingValueEntity, Long> {
  Optional<SettingValueEntity> findByNameAndProviderNameAndProviderKey(
      String name, String providerName, String providerKey);
}