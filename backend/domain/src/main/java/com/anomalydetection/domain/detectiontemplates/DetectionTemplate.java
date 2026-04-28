package com.anomalydetection.domain.detectiontemplates;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Detection template (ABP: DetectionTemplates).
 *
 * <p>Minimal MVP model: a template is a named rule tied to a {@code can_signal_id}.
 * The rule itself is stored as an expression string.
 */
@Entity
@Table(name = "detection_templates")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLDelete(sql = "UPDATE detection_templates SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class DetectionTemplate extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(nullable = false, length = 256)
  private String name;

  @Column(length = 1024)
  private String description;

  @Column(name = "can_signal_id", columnDefinition = "BINARY(16)")
  private UUID canSignalId;

  @Column(name = "expression", columnDefinition = "TEXT")
  private String expression;

  @Column(name = "threshold")
  private Double threshold;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  protected DetectionTemplate() {}

  public DetectionTemplate(UUID id, String name) {
    this.id = id;
    this.name = name;
    this.isActive = true;
  }

  @Override
  public UUID getId() {
    return id;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public UUID getCanSignalId() {
    return canSignalId;
  }

  public void setCanSignalId(UUID canSignalId) {
    this.canSignalId = canSignalId;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public Double getThreshold() {
    return threshold;
  }

  public void setThreshold(Double threshold) {
    this.threshold = threshold;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }
}
