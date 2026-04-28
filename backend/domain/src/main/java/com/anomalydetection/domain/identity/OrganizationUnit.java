package com.anomalydetection.domain.identity;

import com.anomalydetection.domain.base.FullAuditedEntity;
import com.anomalydetection.domain.base.MultiTenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Organization unit (hierarchical) — matching ABP's {@code OrganizationUnit}.
 *
 * <p>Hierarchy is expressed via a {@code code} column using a dotted-path pattern
 * (e.g. {@code 00001.00002}).
 */
@Entity
@Table(name = "organization_units")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLDelete(sql = "UPDATE organization_units SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class OrganizationUnit extends FullAuditedEntity<UUID> implements MultiTenant {

  @Id
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "parent_id", columnDefinition = "BINARY(16)")
  private UUID parentId;

  @Column(name = "code", nullable = false, length = 128)
  private String code;

  @Column(name = "display_name", nullable = false, length = 256)
  private String displayName;

  protected OrganizationUnit() {}

  public OrganizationUnit(UUID id, String code, String displayName) {
    this.id = id;
    this.code = code;
    this.displayName = displayName;
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

  public UUID getParentId() {
    return parentId;
  }

  public void setParentId(UUID parentId) {
    this.parentId = parentId;
  }

  public String getCode() {
    return code;
  }

  public String getDisplayName() {
    return displayName;
  }
}