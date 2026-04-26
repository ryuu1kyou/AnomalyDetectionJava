package com.anomalydetection.infrastructure.multitenancy;

import com.anomalydetection.domain.multitenancy.Tenant;
import com.anomalydetection.domain.multitenancy.TenantRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTenantRepository extends JpaRepository<Tenant, UUID>, TenantRepository {

  Optional<Tenant> findByNormalizedName(String normalizedName);

  boolean existsByNormalizedName(String normalizedName);
}
