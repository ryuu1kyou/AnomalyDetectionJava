package com.anomalydetection.domain.identity;

import com.anomalydetection.domain.base.FullAuditedEntity;
import com.anomalydetection.domain.base.MultiTenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Filter;

/**
 * Application role — matching ABP's {@code IdentityRole}.
 */
@Entity
@Table(name = "roles")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
public class Role extends FullAuditedEntity<UUID> implements MultiTenant {

  @Id
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "name", nullable = false, length = 256)
  private String name;

  @Column(name = "normalized_name", nullable = false, length = 256)
  private String normalizedName;

  @Column(name = "is_static", nullable = false)
  private boolean isStatic;

  @Column(name = "is_default", nullable = false)
  private boolean isDefault;

  protected Role() {}

  public Role(UUID id, String name, String normalizedName) {
    this.id = id;
    this.name = name;
    this.normalizedName = normalizedName;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public UUID getTenantId() {
    return tenantId;
  }

  @Override
  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public String getName() {
    return name;
  }

  public String getNormalizedName() {
    return normalizedName;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public void setStatic(boolean aStatic) {
    isStatic = aStatic;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean aDefault) {
    isDefault = aDefault;
  }
}