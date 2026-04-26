package com.anomalydetection.infrastructure.multitenancy;

import com.anomalydetection.domain.base.MultiTenant;
import jakarta.persistence.PrePersist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA entity listener that automatically injects the current tenant ID into
 * {@link MultiTenant} entities before persist.
 *
 * <p>Registered via {@code @EntityListeners} on each aggregate root that implements
 * {@code MultiTenant}.
 */
public class MultiTenantEntityListener {

  private static final Logger log = LoggerFactory.getLogger(MultiTenantEntityListener.class);

  @PrePersist
  public void prePersist(Object entity) {
    if (entity instanceof MultiTenant mt && mt.getTenantId() == null) {
      CurrentTenantHolder holder = TenantContextHolderProvider.getHolder();
      if (holder != null && holder.isSet()) {
        mt.setTenantId(holder.getTenantId().orElse(null));
      }
    }
  }
}