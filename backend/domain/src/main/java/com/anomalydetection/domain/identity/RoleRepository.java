package com.anomalydetection.domain.identity;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends BaseRepository<Role, UUID> {

  Optional<Role> findByNormalizedName(String normalizedName);

  List<Role> findAllByTenantId(UUID tenantId);
}
