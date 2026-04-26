package com.anomalydetection.domain.multitenancy;

import java.util.Optional;
import java.util.UUID;

public interface ICurrentTenant {

  Optional<UUID> getTenantId();

  boolean isSet();
}
