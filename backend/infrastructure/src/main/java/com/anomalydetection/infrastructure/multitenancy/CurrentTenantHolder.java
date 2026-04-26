package com.anomalydetection.infrastructure.multitenancy;

import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * Request-scoped holder for the current tenant ID.
 *
 * <p>Wired via {@code @Scope("request")} so that any bean can inject this
 * holder without the request scope leaking into singleton beans.
 *
 * <p>Matching ABP's {@code ICurrentTenant}.
 */
@Component
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentTenantHolder implements ICurrentTenant {

  private UUID tenantId;

  /** Returns the current tenant ID, or empty if running in host mode. */
  @Override
  public Optional<UUID> getTenantId() {
    return Optional.ofNullable(tenantId);
  }

  /** Sets the current tenant ID. {@code null} means host mode. */
  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  /** Returns true when a tenant is set. */
  @Override
  public boolean isSet() {
    return tenantId != null;
  }

  /** Returns a no-tenant sentinel for host-administrator operations. */
  public static CurrentTenantHolder none() {
    return new CurrentTenantHolder();
  }
}