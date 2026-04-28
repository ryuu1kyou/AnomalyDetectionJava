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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "anomaly_detection_results")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLDelete(sql = "UPDATE anomaly_detection_results SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class AnomalyDetectionResult extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "detection_logic_id", columnDefinition = "BINARY(16)", nullable = false)
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

  @Column(length = 1000)
  private String description;

  @Column(name = "signal_value")
  private Double signalValue;

  @Column(name = "input_timestamp")
  private Instant inputTimestamp;

  @Enumerated(EnumType.STRING)
  @Column(name = "detection_type", length = 32)
  private AnomalyType detectionType;

  @Column(name = "trigger_condition", length = 500)
  private String triggerCondition;

  @Column(name = "execution_time_ms")
  private Double executionTimeMs;

  @Column(name = "is_validated", nullable = false)
  private boolean isValidated;

  @Column(name = "is_false_positive", nullable = false)
  private boolean isFalsePositive;

  @Column(name = "detection_condition", length = 500)
  private String detectionCondition;

  @Column(name = "detection_duration_ms")
  private Long detectionDurationMs;

  @Enumerated(EnumType.STRING)
  @Column(name = "resolution_status", nullable = false, length = 32)
  private ResolutionStatus resolutionStatus;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @Column(name = "resolved_by", columnDefinition = "BINARY(16)")
  private UUID resolvedBy;

  @Column(name = "resolution_notes", columnDefinition = "LONGTEXT")
  private String resolutionNotes;

  @Enumerated(EnumType.STRING)
  @Column(name = "sharing_level", length = 16)
  private SharingLevel sharingLevel;

  @Column(name = "is_shared", nullable = false)
  private boolean isShared;

  protected AnomalyDetectionResult() {}

  public AnomalyDetectionResult(UUID id, UUID tenantId, UUID detectionLogicId, UUID canSignalId,
      AnomalyLevel anomalyLevel, AnomalyType anomalyType, double confidenceScore, String description) {
    this.id = id;
    this.tenantId = tenantId;
    this.detectionLogicId = detectionLogicId;
    this.canSignalId = canSignalId;
    this.anomalyLevel = anomalyLevel != null ? anomalyLevel : AnomalyLevel.INFO;
    this.anomalyType = anomalyType;
    this.confidenceScore = confidenceScore;
    this.description = description;
    this.resolutionStatus = ResolutionStatus.OPEN;
    this.detectedAt = Instant.now();
    this.sharingLevel = SharingLevel.PRIVATE;
  }

  public void resolve(UUID resolvedBy, String notes) {
    this.resolutionStatus = ResolutionStatus.RESOLVED;
    this.resolvedAt = Instant.now();
    this.resolvedBy = resolvedBy;
    this.resolutionNotes = notes;
    this.isValidated = true;
  }

  public void markAsFalsePositive(UUID resolvedBy, String reason) {
    this.resolutionStatus = ResolutionStatus.FALSE_POSITIVE;
    this.resolvedAt = Instant.now();
    this.resolvedBy = resolvedBy;
    this.resolutionNotes = reason;
    this.isFalsePositive = true;
    this.isValidated = true;
  }

  public void share(SharingLevel level, UUID sharedBy) {
    this.sharingLevel = level;
    this.isShared = level != null && level != SharingLevel.PRIVATE;
  }

  @Override
  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public UUID getDetectionLogicId() { return detectionLogicId; }
  public UUID getCanSignalId() { return canSignalId; }
  public void setCanSignalId(UUID canSignalId) { this.canSignalId = canSignalId; }

  public Instant getDetectedAt() { return detectedAt; }

  public AnomalyLevel getAnomalyLevel() { return anomalyLevel; }
  public void setAnomalyLevel(AnomalyLevel anomalyLevel) { this.anomalyLevel = anomalyLevel; }

  public AnomalyType getAnomalyType() { return anomalyType; }
  public void setAnomalyType(AnomalyType anomalyType) { this.anomalyType = anomalyType; }

  public double getConfidenceScore() { return confidenceScore; }
  public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public Double getSignalValue() { return signalValue; }
  public void setSignalValue(Double signalValue) { this.signalValue = signalValue; }

  public Instant getInputTimestamp() { return inputTimestamp; }
  public void setInputTimestamp(Instant inputTimestamp) { this.inputTimestamp = inputTimestamp; }

  public AnomalyType getDetectionType() { return detectionType; }
  public void setDetectionType(AnomalyType detectionType) { this.detectionType = detectionType; }

  public String getTriggerCondition() { return triggerCondition; }
  public void setTriggerCondition(String triggerCondition) { this.triggerCondition = triggerCondition; }

  public Double getExecutionTimeMs() { return executionTimeMs; }
  public void setExecutionTimeMs(Double executionTimeMs) { this.executionTimeMs = executionTimeMs; }

  public boolean isValidated() { return isValidated; }
  public boolean isFalsePositive() { return isFalsePositive; }

  public String getDetectionCondition() { return detectionCondition; }
  public void setDetectionCondition(String detectionCondition) { this.detectionCondition = detectionCondition; }

  public Long getDetectionDurationMs() { return detectionDurationMs; }
  public void setDetectionDurationMs(Long detectionDurationMs) { this.detectionDurationMs = detectionDurationMs; }

  public ResolutionStatus getResolutionStatus() { return resolutionStatus; }
  public void setResolutionStatus(ResolutionStatus s) { this.resolutionStatus = s; }

  public Instant getResolvedAt() { return resolvedAt; }
  public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

  public UUID getResolvedBy() { return resolvedBy; }
  public void setResolvedBy(UUID resolvedBy) { this.resolvedBy = resolvedBy; }

  public String getResolutionNotes() { return resolutionNotes; }
  public void setResolutionNotes(String notes) { this.resolutionNotes = notes; }

  public SharingLevel getSharingLevel() { return sharingLevel; }
  public boolean isShared() { return isShared; }
}
