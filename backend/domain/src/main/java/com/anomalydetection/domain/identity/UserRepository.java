package com.anomalydetection.domain.identity;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends BaseRepository<User, UUID> {

  Optional<User> findByNormalizedUserName(String normalizedUserName);

  Optional<User> findByNormalizedEmail(String normalizedEmail);

  List<User> findAllByTenantId(UUID tenantId);

  long countByTenantId(UUID tenantId);
}
