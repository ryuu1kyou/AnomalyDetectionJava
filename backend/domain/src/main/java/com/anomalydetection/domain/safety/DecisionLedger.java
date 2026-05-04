package com.anomalydetection.domain.safety;

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

/**
 * Design-intent ledger entry — the "設計意図台帳" from automotive-safety skill 03.
 * Records WHY a decision was made, its assumptions, and constraints, keyed by {@code decisionId}.
 */
@Entity
@Table(name = "decision_ledger")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLDelete(sql = "UPDATE decision_ledger SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class DecisionLedger extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "decision_id", nullable = false, unique = true, length = 64)
  private String decisionId;

  @Column(name = "what_decided", columnDefinition = "LONGTEXT")
  private String whatDecided;

  @Column(name = "why_decided", columnDefinition = "LONGTEXT")
  private String whyDecided;

  @Column(columnDefinition = "LONGTEXT")
  private String assumptions;

  @Column(name = "constraints_text", columnDefinition = "LONGTEXT")
  private String constraintsText;

  /** JSON array of feature IDs referencing this decision. */
  @Column(name = "related_feature_ids", columnDefinition = "LONGTEXT")
  private String relatedFeatureIds;

  /** JSON array of module IDs in scope of this decision. */
  @Column(name = "related_module_ids", columnDefinition = "LONGTEXT")
  private String relatedModuleIds;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private DecisionStatus status;

  @Column(name = "approved_by", columnDefinition = "BINARY(16)")
  private UUID approvedBy;

  @Column(name = "approved_at")
  private Instant approvedAt;

  protected DecisionLedger() {}

  public DecisionLedger(UUID id, String decisionId, String whatDecided) {
    if (decisionId == null || decisionId.isBlank())
      throw new IllegalArgumentException("decision_id is required for a decision ledger entry");
    this.id = id;
    this.decisionId = decisionId;
    this.whatDecided = whatDecided;
    this.status = DecisionStatus.DRAFT;
  }

  public void activate(UUID byUserId) {
    if (status == DecisionStatus.SUPERSEDED)
      throw new IllegalStateException("Cannot activate a superseded decision");
    this.status = DecisionStatus.ACTIVE;
    this.approvedBy = byUserId;
    this.approvedAt = Instant.now();
  }

  public void supersede() {
    this.status = DecisionStatus.SUPERSEDED;
  }

  @Override
  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getDecisionId() { return decisionId; }
  public void setDecisionId(String decisionId) { this.decisionId = decisionId; }

  public String getWhatDecided() { return whatDecided; }
  public void setWhatDecided(String whatDecided) { this.whatDecided = whatDecided; }

  public String getWhyDecided() { return whyDecided; }
  public void setWhyDecided(String whyDecided) { this.whyDecided = whyDecided; }

  public String getAssumptions() { return assumptions; }
  public void setAssumptions(String assumptions) { this.assumptions = assumptions; }

  public String getConstraintsText() { return constraintsText; }
  public void setConstraintsText(String constraintsText) { this.constraintsText = constraintsText; }

  public String getRelatedFeatureIds() { return relatedFeatureIds; }
  public void setRelatedFeatureIds(String relatedFeatureIds) { this.relatedFeatureIds = relatedFeatureIds; }

  public String getRelatedModuleIds() { return relatedModuleIds; }
  public void setRelatedModuleIds(String relatedModuleIds) { this.relatedModuleIds = relatedModuleIds; }

  public DecisionStatus getStatus() { return status; }

  public UUID getApprovedBy() { return approvedBy; }
  public Instant getApprovedAt() { return approvedAt; }
}
