package com.anomalydetection.domain.safety;

import com.anomalydetection.domain.base.FullAuditedEntity;
import com.anomalydetection.shared.safety.DocSyncStatus;
import com.anomalydetection.shared.safety.IfImpact;
import com.anomalydetection.shared.safety.TraceabilityScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "safety_trace_records")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLDelete(sql = "UPDATE safety_trace_records SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class SafetyTraceRecord extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 2000)
  private String description;

  @Column(name = "requirement_id", length = 200)
  private String requirementId;

  @Column(name = "safety_goal_id", length = 200)
  private String safetyGoalId;

  @Column(name = "hazard_analysis_id", length = 200)
  private String hazardAnalysisId;

  @Column(name = "asil_level", length = 8)
  private String asilLevel;

  @Column(name = "detection_logic_id", columnDefinition = "BINARY(16)")
  private UUID detectionLogicId;

  @Column(name = "project_id", columnDefinition = "BINARY(16)")
  private UUID projectId;

  @Column(length = 20)
  private String version;

  @Enumerated(EnumType.STRING)
  @Column(name = "approval_status", nullable = false, length = 32)
  private SafetyApprovalStatus approvalStatus;

  @Column(name = "submitted_at")
  private Instant submittedAt;

  @Column(name = "submitted_by", columnDefinition = "BINARY(16)")
  private UUID submittedBy;

  @Column(name = "approved_at")
  private Instant approvedAt;

  @Column(name = "approved_by", columnDefinition = "BINARY(16)")
  private UUID approvedBy;

  @Column(name = "rejected_at")
  private Instant rejectedAt;

  @Column(name = "rejected_by", columnDefinition = "BINARY(16)")
  private UUID rejectedBy;

  @Column(name = "approval_comments", columnDefinition = "LONGTEXT")
  private String approvalComments;

  @Column(name = "related_documents", columnDefinition = "LONGTEXT")
  private String relatedDocuments;

  @Column(columnDefinition = "LONGTEXT")
  private String verifications;

  @Column(columnDefinition = "LONGTEXT")
  private String validations;

  @Column(name = "lifecycle_events", columnDefinition = "LONGTEXT")
  private String lifecycleEvents;

  @Column(name = "change_requests", columnDefinition = "LONGTEXT")
  private String changeRequests;

  // ── Traceability keys (automotive-safety skill) ──────────────────────────
  @Column(name = "feature_id", length = 64)
  private String featureId;

  @Column(name = "decision_id", length = 64)
  private String decisionId;

  @Column(name = "change_id", length = 64)
  private String changeId;

  @Enumerated(EnumType.STRING)
  @Column(name = "if_impact", length = 16)
  private IfImpact ifImpact;

  @Column(name = "unknown_until")
  private LocalDate unknownUntil;

  @Column(name = "unknown_owner_id", columnDefinition = "BINARY(16)")
  private UUID unknownOwnerId;

  @Column(name = "design_rationale", columnDefinition = "LONGTEXT")
  private String designRationale;

  @Column(name = "assumption", columnDefinition = "LONGTEXT")
  private String assumption;

  @Column(name = "constraint_text", columnDefinition = "LONGTEXT")
  private String constraintText;

  @Enumerated(EnumType.STRING)
  @Column(name = "doc_sync_status", length = 16)
  private DocSyncStatus docSyncStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "scope", length = 16)
  private TraceabilityScope scope;

  @Column(name = "applicability", length = 255)
  private String applicability;

  protected SafetyTraceRecord() {}

  public SafetyTraceRecord(UUID id, String name, String asilLevel, String featureId) {
    if (featureId == null || featureId.isBlank())
      throw new IllegalArgumentException("feature_id is required for safety trace records");
    this.id = id;
    this.name = name;
    this.asilLevel = asilLevel;
    this.featureId = featureId;
    this.approvalStatus = SafetyApprovalStatus.DRAFT;
    this.version = "1.0";
    this.ifImpact = IfImpact.UNKNOWN;
    this.docSyncStatus = DocSyncStatus.NOT_REQUIRED;
    this.scope = TraceabilityScope.PLATFORM;
  }

  public void submit(UUID byUserId) {
    if (approvalStatus != SafetyApprovalStatus.DRAFT) {
      throw new IllegalStateException(
          "Only DRAFT records can be submitted, current status: " + approvalStatus);
    }
    if (ifImpact == IfImpact.UNKNOWN
        && (unknownUntil == null || unknownOwnerId == null)) {
      throw new IllegalStateException(
          "Cannot submit: if_impact is UNKNOWN but unknown_until or unknown_owner_id is not set");
    }
    this.approvalStatus = SafetyApprovalStatus.SUBMITTED;
    this.submittedAt = Instant.now();
    this.submittedBy = byUserId;
  }

  public void startReview() {
    if (approvalStatus != SafetyApprovalStatus.SUBMITTED) {
      throw new IllegalStateException(
          "Only SUBMITTED records can start review, current status: " + approvalStatus);
    }
    this.approvalStatus = SafetyApprovalStatus.UNDER_REVIEW;
  }

  public void approve(UUID byUserId, String comments) {
    if (approvalStatus != SafetyApprovalStatus.SUBMITTED
        && approvalStatus != SafetyApprovalStatus.UNDER_REVIEW) {
      throw new IllegalStateException(
          "Only SUBMITTED or UNDER_REVIEW records can be approved, current status: " + approvalStatus);
    }
    this.approvalStatus = SafetyApprovalStatus.APPROVED;
    this.approvedAt = Instant.now();
    this.approvedBy = byUserId;
    this.approvalComments = comments;
  }

  public void reject(UUID byUserId, String comments) {
    if (approvalStatus != SafetyApprovalStatus.SUBMITTED
        && approvalStatus != SafetyApprovalStatus.UNDER_REVIEW) {
      throw new IllegalStateException(
          "Only SUBMITTED or UNDER_REVIEW records can be rejected, current status: " + approvalStatus);
    }
    this.approvalStatus = SafetyApprovalStatus.REJECTED;
    this.rejectedAt = Instant.now();
    this.rejectedBy = byUserId;
    this.approvalComments = comments;
  }

  public void updateAsilLevel(String newLevel) {
    this.asilLevel = newLevel;
    if (this.approvalStatus == SafetyApprovalStatus.APPROVED) {
      this.approvalStatus = SafetyApprovalStatus.DRAFT;
    }
  }

  @Override
  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getRequirementId() { return requirementId; }
  public void setRequirementId(String requirementId) { this.requirementId = requirementId; }

  public String getSafetyGoalId() { return safetyGoalId; }
  public void setSafetyGoalId(String safetyGoalId) { this.safetyGoalId = safetyGoalId; }

  public String getHazardAnalysisId() { return hazardAnalysisId; }
  public void setHazardAnalysisId(String hazardAnalysisId) { this.hazardAnalysisId = hazardAnalysisId; }

  public String getAsilLevel() { return asilLevel; }
  public void setAsilLevel(String asilLevel) { this.asilLevel = asilLevel; }

  public UUID getDetectionLogicId() { return detectionLogicId; }
  public void setDetectionLogicId(UUID detectionLogicId) { this.detectionLogicId = detectionLogicId; }

  public UUID getProjectId() { return projectId; }
  public void setProjectId(UUID projectId) { this.projectId = projectId; }

  public String getVersion() { return version; }
  public void setVersion(String version) { this.version = version; }

  public SafetyApprovalStatus getApprovalStatus() { return approvalStatus; }

  public Instant getSubmittedAt() { return submittedAt; }
  public UUID getSubmittedBy() { return submittedBy; }
  public Instant getApprovedAt() { return approvedAt; }
  public UUID getApprovedBy() { return approvedBy; }
  public Instant getRejectedAt() { return rejectedAt; }
  public UUID getRejectedBy() { return rejectedBy; }
  public String getApprovalComments() { return approvalComments; }

  public String getRelatedDocuments() { return relatedDocuments; }
  public void setRelatedDocuments(String relatedDocuments) { this.relatedDocuments = relatedDocuments; }

  public String getVerifications() { return verifications; }
  public void setVerifications(String verifications) { this.verifications = verifications; }

  public String getValidations() { return validations; }
  public void setValidations(String validations) { this.validations = validations; }

  public String getLifecycleEvents() { return lifecycleEvents; }
  public void setLifecycleEvents(String lifecycleEvents) { this.lifecycleEvents = lifecycleEvents; }

  public String getChangeRequests() { return changeRequests; }
  public void setChangeRequests(String changeRequests) { this.changeRequests = changeRequests; }

  // ── Traceability key accessors ────────────────────────────────────────────
  public String getFeatureId() { return featureId; }
  public void setFeatureId(String featureId) { this.featureId = featureId; }

  public String getDecisionId() { return decisionId; }
  public void setDecisionId(String decisionId) { this.decisionId = decisionId; }

  public String getChangeId() { return changeId; }
  public void setChangeId(String changeId) { this.changeId = changeId; }

  public IfImpact getIfImpact() { return ifImpact; }
  public void setIfImpact(IfImpact ifImpact) { this.ifImpact = ifImpact; }

  public LocalDate getUnknownUntil() { return unknownUntil; }
  public void setUnknownUntil(LocalDate unknownUntil) { this.unknownUntil = unknownUntil; }

  public UUID getUnknownOwnerId() { return unknownOwnerId; }
  public void setUnknownOwnerId(UUID unknownOwnerId) { this.unknownOwnerId = unknownOwnerId; }

  public String getDesignRationale() { return designRationale; }
  public void setDesignRationale(String designRationale) { this.designRationale = designRationale; }

  public String getAssumption() { return assumption; }
  public void setAssumption(String assumption) { this.assumption = assumption; }

  public String getConstraintText() { return constraintText; }
  public void setConstraintText(String constraintText) { this.constraintText = constraintText; }

  public DocSyncStatus getDocSyncStatus() { return docSyncStatus; }
  public void setDocSyncStatus(DocSyncStatus docSyncStatus) { this.docSyncStatus = docSyncStatus; }

  public TraceabilityScope getScope() { return scope; }
  public void setScope(TraceabilityScope scope) { this.scope = scope; }

  public String getApplicability() { return applicability; }
  public void setApplicability(String applicability) { this.applicability = applicability; }
}
