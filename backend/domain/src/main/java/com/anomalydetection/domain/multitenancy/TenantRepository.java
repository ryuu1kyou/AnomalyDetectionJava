package com.anomalydetection.domain.multitenancy;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends BaseRepository<Tenant, UUID> {

  Optional<Tenant> findByNormalizedName(String normalizedName);

  boolean existsByNormalizedName(String normalizedName);
}
