package com.anomalydetection.domain.oemtraceability;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "oem_customizations")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLDelete(sql = "UPDATE oem_customizations SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class OemCustomization extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "entity_id", nullable = false, length = 200)
  private String entityId;

  @Column(name = "entity_type", nullable = false, length = 100)
  private String entityType;

  @Column(name = "oem_code", nullable = false, length = 100)
  private String oemCode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private OemCustomizationType type;

  @Column(name = "custom_parameters", columnDefinition = "LONGTEXT")
  private String customParameters;

  @Column(name = "original_parameters", columnDefinition = "LONGTEXT")
  private String originalParameters;

  @Column(name = "customization_reason", length = 2000)
  private String customizationReason;

  @Column(name = "approved_by", columnDefinition = "BINARY(16)")
  private UUID approvedBy;

  @Column(name = "approved_at")
  private Instant approvedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private OemCustomizationStatus status;

  @Column(name = "approval_notes", columnDefinition = "LONGTEXT")
  private String approvalNotes;

  protected OemCustomization() {}

  public OemCustomization(UUID id, String entityId, String entityType, String oemCode,
      OemCustomizationType type) {
    this.id = id;
    this.entityId = entityId;
    this.entityType = entityType;
    this.oemCode = oemCode;
    this.type = type;
    this.status = OemCustomizationStatus.DRAFT;
  }

  public void submitForApproval() {
    this.status = OemCustomizationStatus.PENDING_APPROVAL;
  }

  public void approve(UUID approvedBy, String notes) {
    this.status = OemCustomizationStatus.APPROVED;
    this.approvedBy = approvedBy;
    this.approvedAt = Instant.now();
    this.approvalNotes = notes;
  }

  public void reject(UUID rejectedBy, String notes) {
    this.status = OemCustomizationStatus.REJECTED;
    this.approvedBy = rejectedBy;
    this.approvedAt = Instant.now();
    this.approvalNotes = notes;
  }

  public void markObsolete() {
    this.status = OemCustomizationStatus.OBSOLETE;
  }

  public void updateCustomParameters(String customParameters) {
    this.customParameters = customParameters;
    if (this.status == OemCustomizationStatus.PENDING_APPROVAL) {
      this.status = OemCustomizationStatus.DRAFT;
    }
  }

  @Override
  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getEntityId() { return entityId; }
  public String getEntityType() { return entityType; }
  public String getOemCode() { return oemCode; }
  public OemCustomizationType getType() { return type; }

  public String getCustomParameters() { return customParameters; }
  public void setCustomParameters(String customParameters) { this.customParameters = customParameters; }

  public String getOriginalParameters() { return originalParameters; }
  public void setOriginalParameters(String originalParameters) { this.originalParameters = originalParameters; }

  public String getCustomizationReason() { return customizationReason; }
  public void setCustomizationReason(String customizationReason) { this.customizationReason = customizationReason; }

  public UUID getApprovedBy() { return approvedBy; }
  public Instant getApprovedAt() { return approvedAt; }

  public OemCustomizationStatus getStatus() { return status; }
  public String getApprovalNotes() { return approvalNotes; }
}
