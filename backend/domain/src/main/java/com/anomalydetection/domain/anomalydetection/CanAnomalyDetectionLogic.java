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
@Table(name = "can_anomaly_detection_logics")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLDelete(sql = "UPDATE can_anomaly_detection_logics SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class CanAnomalyDetectionLogic extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  // Identity (flattened)
  @Column(nullable = false, length = 100)
  private String name;

  @Column(name = "version", nullable = false, length = 20)
  private String version;

  @Column(name = "oem_code", length = 100)
  private String oemCode;

  // Specification (flattened)
  @Enumerated(EnumType.STRING)
  @Column(name = "anomaly_type", length = 32)
  private AnomalyType anomalyType;

  @Column(length = 1000)
  private String description;

  @Column(name = "target_system_type", length = 32)
  private String targetSystemType;

  @Enumerated(EnumType.STRING)
  @Column(name = "complexity", length = 16)
  private LogicComplexity complexity;

  @Column(name = "requirements", length = 2000)
  private String requirements;

  // Implementation (flattened)
  @Enumerated(EnumType.STRING)
  @Column(name = "implementation_type", length = 32)
  private ImplementationType implementationType;

  @Column(name = "implementation_content", columnDefinition = "LONGTEXT")
  private String implementationContent;

  @Column(name = "implementation_language", length = 50)
  private String implementationLanguage;

  @Column(name = "implementation_entry_point", length = 200)
  private String implementationEntryPoint;

  // Safety (flattened)
  @Enumerated(EnumType.STRING)
  @Column(name = "asil_level", length = 8)
  private AsilLevel asilLevel;

  @Column(name = "safety_requirement_id", length = 100)
  private String safetyRequirementId;

  @Column(name = "safety_goal_id", length = 100)
  private String safetyGoalId;

  @Column(name = "hazard_analysis_id", length = 100)
  private String hazardAnalysisId;

  // Status
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private DetectionLogicStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "sharing_level", nullable = false, length = 16)
  private SharingLevel sharingLevel;

  @Column(name = "source_logic_id", columnDefinition = "BINARY(16)")
  private UUID sourceLogicId;

  @Column(name = "vehicle_phase_id", columnDefinition = "BINARY(16)")
  private UUID vehiclePhaseId;

  @Column(name = "approved_at")
  private Instant approvedAt;

  @Column(name = "approved_by", columnDefinition = "BINARY(16)")
  private UUID approvedBy;

  @Column(name = "approval_notes", columnDefinition = "LONGTEXT")
  private String approvalNotes;

  // Execution stats
  @Column(name = "execution_count", nullable = false)
  private int executionCount;

  @Column(name = "last_executed_at")
  private Instant lastExecutedAt;

  @Column(name = "last_execution_time_ms")
  private Double lastExecutionTimeMs;

  protected CanAnomalyDetectionLogic() {}

  public CanAnomalyDetectionLogic(UUID id, String name, String version) {
    this.id = id;
    this.name = name;
    this.version = version;
    this.status = DetectionLogicStatus.DRAFT;
    this.sharingLevel = SharingLevel.PRIVATE;
    this.asilLevel = AsilLevel.QM;
    this.complexity = LogicComplexity.SIMPLE;
    this.implementationType = ImplementationType.CONFIGURATION;
    this.executionCount = 0;
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

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getOemCode() {
    return oemCode;
  }

  public void setOemCode(String oemCode) {
    this.oemCode = oemCode;
  }

  public AnomalyType getAnomalyType() {
    return anomalyType;
  }

  public void setAnomalyType(AnomalyType anomalyType) {
    this.anomalyType = anomalyType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTargetSystemType() {
    return targetSystemType;
  }

  public void setTargetSystemType(String targetSystemType) {
    this.targetSystemType = targetSystemType;
  }

  public LogicComplexity getComplexity() {
    return complexity;
  }

  public void setComplexity(LogicComplexity complexity) {
    this.complexity = complexity;
  }

  public String getRequirements() {
    return requirements;
  }

  public void setRequirements(String requirements) {
    this.requirements = requirements;
  }

  public ImplementationType getImplementationType() {
    return implementationType;
  }

  public void setImplementationType(ImplementationType implementationType) {
    this.implementationType = implementationType;
  }

  public String getImplementationContent() {
    return implementationContent;
  }

  public void setImplementationContent(String implementationContent) {
    this.implementationContent = implementationContent;
  }

  public String getImplementationLanguage() {
    return implementationLanguage;
  }

  public void setImplementationLanguage(String implementationLanguage) {
    this.implementationLanguage = implementationLanguage;
  }

  public String getImplementationEntryPoint() {
    return implementationEntryPoint;
  }

  public void setImplementationEntryPoint(String implementationEntryPoint) {
    this.implementationEntryPoint = implementationEntryPoint;
  }

  public AsilLevel getAsilLevel() {
    return asilLevel;
  }

  public void setAsilLevel(AsilLevel asilLevel) {
    this.asilLevel = asilLevel;
  }

  public String getSafetyRequirementId() {
    return safetyRequirementId;
  }

  public void setSafetyRequirementId(String safetyRequirementId) {
    this.safetyRequirementId = safetyRequirementId;
  }

  public String getSafetyGoalId() {
    return safetyGoalId;
  }

  public void setSafetyGoalId(String safetyGoalId) {
    this.safetyGoalId = safetyGoalId;
  }

  public String getHazardAnalysisId() {
    return hazardAnalysisId;
  }

  public void setHazardAnalysisId(String hazardAnalysisId) {
    this.hazardAnalysisId = hazardAnalysisId;
  }

  public DetectionLogicStatus getStatus() {
    return status;
  }

  public void setStatus(DetectionLogicStatus status) {
    this.status = status;
  }

  public SharingLevel getSharingLevel() {
    return sharingLevel;
  }

  public void setSharingLevel(SharingLevel sharingLevel) {
    this.sharingLevel = sharingLevel;
  }

  public UUID getSourceLogicId() {
    return sourceLogicId;
  }

  public void setSourceLogicId(UUID sourceLogicId) {
    this.sourceLogicId = sourceLogicId;
  }

  public UUID getVehiclePhaseId() {
    return vehiclePhaseId;
  }

  public void setVehiclePhaseId(UUID vehiclePhaseId) {
    this.vehiclePhaseId = vehiclePhaseId;
  }

  public Instant getApprovedAt() {
    return approvedAt;
  }

  public UUID getApprovedBy() {
    return approvedBy;
  }

  public String getApprovalNotes() {
    return approvalNotes;
  }

  public int getExecutionCount() {
    return executionCount;
  }

  public Instant getLastExecutedAt() {
    return lastExecutedAt;
  }

  public Double getLastExecutionTimeMs() {
    return lastExecutionTimeMs;
  }

  public void submitForApproval() {
    if (status != DetectionLogicStatus.DRAFT) {
      throw new IllegalStateException("Only DRAFT logic can be submitted for approval");
    }
    this.status = DetectionLogicStatus.PENDING_APPROVAL;
  }

  public void approve(UUID approvedBy, String notes) {
    if (status != DetectionLogicStatus.PENDING_APPROVAL) {
      throw new IllegalStateException("Only PENDING_APPROVAL logic can be approved");
    }
    this.status = DetectionLogicStatus.APPROVED;
    this.approvedAt = Instant.now();
    this.approvedBy = approvedBy;
    this.approvalNotes = notes;
  }

  public void reject(String reason) {
    if (status != DetectionLogicStatus.PENDING_APPROVAL) {
      throw new IllegalStateException("Only PENDING_APPROVAL logic can be rejected");
    }
    this.status = DetectionLogicStatus.REJECTED;
    this.approvalNotes = reason;
  }

  public void deprecate(String reason) {
    this.status = DetectionLogicStatus.DEPRECATED;
    this.approvalNotes = (approvalNotes == null ? "" : approvalNotes + "; ") + "Deprecated: " + reason;
  }

  public void recordExecution(double executionTimeMs) {
    this.executionCount++;
    this.lastExecutedAt = Instant.now();
    this.lastExecutionTimeMs = executionTimeMs;
  }
}
