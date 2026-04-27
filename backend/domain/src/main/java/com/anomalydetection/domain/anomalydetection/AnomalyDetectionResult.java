package com.anomalydetection.domain.anomalydetection;

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
@Table(name = "anomaly_detection_results")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLRestriction("is_deleted = false")
public class AnomalyDetectionResult extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "detection_logic_id", columnDefinition = "BINARY(16)")
  private UUID detectionLogicId;

  @Column(name = "can_signal_id", columnDefinition = "BINARY(16)")
  private UUID canSignalId;

  @Column(name = "detected_at", nullable = false)
  private Instant detectedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "anomaly_level", nullable = false, length = 16)
  private AnomalyLevel anomalyLevel;

  @Enumerated(EnumType.STRING)
  @Column(name = "anomaly_type", length = 32)
  private AnomalyType anomalyType;

  @Column(name = "confidence_score", nullable = false)
  private double confidenceScore;

  @Column(nullable = false, length = 1000)
  private String description;

  // InputData (flattened)
  @Column(name = "signal_value")
  private Double signalValue;

  @Column(name = "input_timestamp")
  private Instant inputTimestamp;

  @Column(name = "input_additional_data", columnDefinition = "LONGTEXT")
  private String inputAdditionalData;

  // DetectionDetails (flattened)
  @Enumerated(EnumType.STRING)
  @Column(name = "detection_type", length = 32)
  private AnomalyType detectionType;

  @Column(name = "trigger_condition", length = 500)
  private String triggerCondition;

  @Column(name = "detection_params", columnDefinition = "LONGTEXT")
  private String detectionParams;

  @Column(name = "execution_time_ms")
  private Double executionTimeMs;

  // Validation
  @Column(name = "is_validated", nullable = false)
  private boolean isValidated;

  @Column(name = "is_false_positive", nullable = false)
  private boolean isFalsePositive;

  @Column(name = "validation_notes", columnDefinition = "LONGTEXT")
  private String validationNotes;

  @Column(name = "detection_condition", length = 500)
  private String detectionCondition;

  @Column(name = "detection_duration_ms")
  private Long detectionDurationMs;

  // Resolution
  @Enumerated(EnumType.STRING)
  @Column(name = "resolution_status", nullable = false, length = 32)
  private ResolutionStatus resolutionStatus;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @Column(name = "resolved_by", columnDefinition = "BINARY(16)")
  private UUID resolvedBy;

  @Column(name = "resolution_notes", columnDefinition = "LONGTEXT")
  private String resolutionNotes;

  // Sharing
  @Enumerated(EnumType.STRING)
  @Column(name = "sharing_level", nullable = false, length = 16)
  private SharingLevel sharingLevel;

  @Column(name = "is_shared", nullable = false)
  private boolean isShared;

  @Column(name = "shared_at")
  private Instant sharedAt;

  @Column(name = "shared_by", columnDefinition = "BINARY(16)")
  private UUID sharedBy;

  protected AnomalyDetectionResult() {}

  public AnomalyDetectionResult(
      UUID id,
      UUID tenantId,
      UUID detectionLogicId,
      UUID canSignalId,
      AnomalyLevel anomalyLevel,
      AnomalyType anomalyType,
      double confidenceScore,
      String description) {
    this.id = id;
    this.tenantId = tenantId;
    this.detectionLogicId = detectionLogicId;
    this.canSignalId = canSignalId;
    this.detectedAt = Instant.now();
    this.anomalyLevel = anomalyLevel;
    this.anomalyType = anomalyType;
    this.confidenceScore = confidenceScore;
    this.description = description;
    this.resolutionStatus = ResolutionStatus.OPEN;
    this.sharingLevel = SharingLevel.PRIVATE;
    this.isValidated = false;
    this.isFalsePositive = false;
    this.isShared = false;
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

  public UUID getDetectionLogicId() {
    return detectionLogicId;
  }

  public UUID getCanSignalId() {
    return canSignalId;
  }

  public Instant getDetectedAt() {
    return detectedAt;
  }

  public AnomalyLevel getAnomalyLevel() {
    return anomalyLevel;
  }

  public void setAnomalyLevel(AnomalyLevel anomalyLevel) {
    this.anomalyLevel = anomalyLevel;
  }

  public AnomalyType getAnomalyType() {
    return anomalyType;
  }

  public void setAnomalyType(AnomalyType anomalyType) {
    this.anomalyType = anomalyType;
  }

  public double getConfidenceScore() {
    return confidenceScore;
  }

  public void setConfidenceScore(double confidenceScore) {
    this.confidenceScore = confidenceScore;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Double getSignalValue() {
    return signalValue;
  }

  public void setSignalValue(Double signalValue) {
    this.signalValue = signalValue;
  }

  public Instant getInputTimestamp() {
    return inputTimestamp;
  }

  public void setInputTimestamp(Instant inputTimestamp) {
    this.inputTimestamp = inputTimestamp;
  }

  public String getInputAdditionalData() {
    return inputAdditionalData;
  }

  public void setInputAdditionalData(String inputAdditionalData) {
    this.inputAdditionalData = inputAdditionalData;
  }

  public AnomalyType getDetectionType() {
    return detectionType;
  }

  public void setDetectionType(AnomalyType detectionType) {
    this.detectionType = detectionType;
  }

  public String getTriggerCondition() {
    return triggerCondition;
  }

  public void setTriggerCondition(String triggerCondition) {
    this.triggerCondition = triggerCondition;
  }

  public String getDetectionParams() {
    return detectionParams;
  }

  public void setDetectionParams(String detectionParams) {
    this.detectionParams = detectionParams;
  }

  public Double getExecutionTimeMs() {
    return executionTimeMs;
  }

  public void setExecutionTimeMs(Double executionTimeMs) {
    this.executionTimeMs = executionTimeMs;
  }

  public boolean isValidated() {
    return isValidated;
  }

  public boolean isFalsePositive() {
    return isFalsePositive;
  }

  public String getValidationNotes() {
    return validationNotes;
  }

  public String getDetectionCondition() {
    return detectionCondition;
  }

  public void setDetectionCondition(String detectionCondition) {
    this.detectionCondition = detectionCondition;
  }

  public Long getDetectionDurationMs() {
    return detectionDurationMs;
  }

  public void setDetectionDurationMs(Long detectionDurationMs) {
    this.detectionDurationMs = detectionDurationMs;
  }

  public ResolutionStatus getResolutionStatus() {
    return resolutionStatus;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
  }

  public UUID getResolvedBy() {
    return resolvedBy;
  }

  public String getResolutionNotes() {
    return resolutionNotes;
  }

  public SharingLevel getSharingLevel() {
    return sharingLevel;
  }

  public boolean isShared() {
    return isShared;
  }

  public Instant getSharedAt() {
    return sharedAt;
  }

  public UUID getSharedBy() {
    return sharedBy;
  }

  public void markAsInvestigating(UUID investigatedBy) {
    if (resolutionStatus == ResolutionStatus.RESOLVED) {
      throw new IllegalStateException("Cannot investigate resolved result");
    }
    this.resolutionStatus = ResolutionStatus.IN_PROGRESS;
    this.resolvedBy = investigatedBy;
  }

  public void markAsFalsePositive(UUID resolvedBy, String reason) {
    this.resolutionStatus = ResolutionStatus.FALSE_POSITIVE;
    this.isFalsePositive = true;
    this.resolvedAt = Instant.now();
    this.resolvedBy = resolvedBy;
    appendResolutionNote("Marked as false positive: " + reason);
  }

  public void resolve(UUID resolvedBy, String resolution) {
    this.resolutionStatus = ResolutionStatus.RESOLVED;
    this.resolvedAt = Instant.now();
    this.resolvedBy = resolvedBy;
    appendResolutionNote("Resolved: " + resolution);
  }

  public void reopen(String reason) {
    if (resolutionStatus == ResolutionStatus.OPEN) {
      throw new IllegalStateException("Result is already open");
    }
    this.resolutionStatus = ResolutionStatus.REOPENED;
    this.resolvedAt = null;
    appendResolutionNote("Reopened: " + reason);
  }

  public void validate(boolean isValid, String notes) {
    this.isValidated = true;
    if (!isValid) {
      this.isFalsePositive = true;
    }
    if (notes != null && !notes.isEmpty()) {
      appendValidationNote(notes);
    }
  }

  public void share(SharingLevel level, UUID sharedBy) {
    if (resolutionStatus == ResolutionStatus.FALSE_POSITIVE) {
      throw new IllegalStateException("Cannot share false positive results");
    }
    this.sharingLevel = level;
    this.isShared = true;
    this.sharedAt = Instant.now();
    this.sharedBy = sharedBy;
  }

  public void revokeSharing() {
    this.sharingLevel = SharingLevel.PRIVATE;
    this.isShared = false;
    this.sharedAt = null;
    this.sharedBy = null;
  }

  private void appendResolutionNote(String note) {
    this.resolutionNotes = resolutionNotes == null || resolutionNotes.isEmpty()
        ? note
        : resolutionNotes + "\n" + note;
  }

  private void appendValidationNote(String note) {
    this.validationNotes = validationNotes == null || validationNotes.isEmpty()
        ? note
        : validationNotes + "\n" + note;
  }
}
