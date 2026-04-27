package com.anomalydetection.domain.base;

import java.util.UUID;

/**
 * Interface for multi-tenant entities, matching ABP {@code IMultiTenant}.
 *
 * <p>Entities implementing this interface receive automatic {@code tenant_id} filtering
 * via Hibernate {@code @Filter}. A {@code null} tenant ID represents OEM-shared data
 * visible to all tenants.
 */
public interface MultiTenant {

  /** Returns the tenant ID, or {@code null} for global/oem-shared data. */
  UUID getTenantId();

  /** Sets the tenant ID. Use {@code null} for global data. */
  void setTenantId(UUID tenantId);
}