package com.anomalydetection.domain.permissions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Records that a provider (role/user/global) holds a named permission.
 * Matches ABP PermissionGrant.
 */
@Entity
@Table(name = "permission_grants")
public class PermissionGrant {

  @Id
  @Column(length = 36)
  private String id;

  @Column(nullable = false, length = 128)
  private String name;

  /** "R" = role, "U" = user, "G" = global */
  @Column(name = "provider_name", nullable = false, length = 64)
  private String providerName;

  /** Role name, user UUID string, or null for global */
  @Column(name = "provider_key", length = 64)
  private String providerKey;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  protected PermissionGrant() {}

  public PermissionGrant(String name, String providerName, String providerKey, UUID tenantId) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
    this.providerName = providerName;
    this.providerKey = providerKey;
    this.tenantId = tenantId;
  }

  public String getId() { return id; }
  public String getName() { return name; }
  public String getProviderName() { return providerName; }
  public String getProviderKey() { return providerKey; }
  public UUID getTenantId() { return tenantId; }
}