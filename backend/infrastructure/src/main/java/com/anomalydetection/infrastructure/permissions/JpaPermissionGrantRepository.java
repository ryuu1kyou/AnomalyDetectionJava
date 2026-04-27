package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.domain.permissions.PermissionGrant;
import com.anomalydetection.domain.permissions.PermissionGrantRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPermissionGrantRepository
    extends PermissionGrantRepository, JpaRepository<PermissionGrant, String> {}