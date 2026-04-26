package com.anomalydetection.infrastructure.identity;

import com.anomalydetection.domain.identity.User;
import com.anomalydetection.domain.identity.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID>, UserRepository {

  Optional<User> findByNormalizedUserName(String normalizedUserName);

  Optional<User> findByNormalizedEmail(String normalizedEmail);

  List<User> findAllByTenantId(UUID tenantId);

  long countByTenantId(UUID tenantId);
}
