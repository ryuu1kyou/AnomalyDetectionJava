package com.anomalydetection.infrastructure.features;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaFeatureValueRepository extends JpaRepository<FeatureValueEntity, Long> {
  Optional<FeatureValueEntity> findByNameAndProviderNameAndProviderKey(
      String name, String providerName, String providerKey);
}