package com.anomalydetection.domain.multitenancy;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "tenants")
@SQLDelete(sql = "UPDATE tenants SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Tenant extends FullAuditedEntity<UUID> {

  @Id
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "name", nullable = false, length = 256)
  private String name;

  @Column(name = "normalized_name", nullable = false, length = 256)
  private String normalizedName;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  protected Tenant() {}

  public Tenant(UUID id, String name) {
    this.id = id;
    this.name = name;
    this.normalizedName = name.toUpperCase();
    this.isActive = true;
  }

  @Override public UUID getId() { return id; }

  public String getName() { return name; }

  public void setName(String name) {
    this.name = name;
    this.normalizedName = name.toUpperCase();
  }

  public String getNormalizedName() { return normalizedName; }
  public boolean isActive() { return isActive; }
  public void setActive(boolean active) { isActive = active; }
}
