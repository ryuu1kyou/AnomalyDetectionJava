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
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "oem_approvals")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLRestriction("is_deleted = false")
public class OemApproval extends FullAuditedEntity<UUID> {

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
  private OemApprovalType type;

  @Column(name = "requested_by", columnDefinition = "BINARY(16)")
  private UUID requestedBy;

  @Column(name = "requested_at")
  private Instant requestedAt;

  @Column(name = "approved_by", columnDefinition = "BINARY(16)")
  private UUID approvedBy;

  @Column(name = "approved_at")
  private Instant approvedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private OemApprovalStatus status;

  @Column(name = "approval_reason", length = 2000)
  private String approvalReason;

  @Column(name = "approval_notes", columnDefinition = "LONGTEXT")
  private String approvalNotes;

  @Column(name = "approval_data", columnDefinition = "LONGTEXT")
  private String approvalData;

  @Column(name = "due_date")
  private Instant dueDate;

  @Column(nullable = false)
  private int priority;

  protected OemApproval() {}

  public OemApproval(UUID id, String entityId, String entityType, String oemCode,
      OemApprovalType type) {
    this.id = id;
    this.entityId = entityId;
    this.entityType = entityType;
    this.oemCode = oemCode;
    this.type = type;
    this.status = OemApprovalStatus.PENDING;
    this.requestedAt = Instant.now();
    this.priority = 2;
  }

  public void approve(UUID approvedBy, String notes) {
    this.status = OemApprovalStatus.APPROVED;
    this.approvedBy = approvedBy;
    this.approvedAt = Instant.now();
    this.approvalNotes = notes;
  }

  public void reject(UUID rejectedBy, String notes) {
    this.status = OemApprovalStatus.REJECTED;
    this.approvedBy = rejectedBy;
    this.approvedAt = Instant.now();
    this.approvalNotes = notes;
  }

  public void cancel(UUID cancelledBy, String reason) {
    this.status = OemApprovalStatus.CANCELLED;
    this.approvalNotes = reason;
  }

  public boolean isOverdue() {
    return dueDate != null && Instant.now().isAfter(dueDate)
        && status == OemApprovalStatus.PENDING;
  }

  @Override
  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getEntityId() { return entityId; }
  public String getEntityType() { return entityType; }
  public String getOemCode() { return oemCode; }
  public OemApprovalType getType() { return type; }

  public UUID getRequestedBy() { return requestedBy; }
  public void setRequestedBy(UUID requestedBy) { this.requestedBy = requestedBy; }

  public Instant getRequestedAt() { return requestedAt; }
  public UUID getApprovedBy() { return approvedBy; }
  public Instant getApprovedAt() { return approvedAt; }

  public OemApprovalStatus getStatus() { return status; }

  public String getApprovalReason() { return approvalReason; }
  public void setApprovalReason(String approvalReason) { this.approvalReason = approvalReason; }

  public String getApprovalNotes() { return approvalNotes; }

  public String getApprovalData() { return approvalData; }
  public void setApprovalData(String approvalData) { this.approvalData = approvalData; }

  public Instant getDueDate() { return dueDate; }
  public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }

  public int getPriority() { return priority; }
  public void setPriority(int priority) { this.priority = Math.max(1, Math.min(4, priority)); }
}
