package com.anomalydetection.infrastructure.identity;

import com.anomalydetection.domain.identity.Role;
import com.anomalydetection.domain.identity.RoleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRoleRepository extends JpaRepository<Role, UUID>, RoleRepository {

  Optional<Role> findByNormalizedName(String normalizedName);

  List<Role> findAllByTenantId(UUID tenantId);
}
