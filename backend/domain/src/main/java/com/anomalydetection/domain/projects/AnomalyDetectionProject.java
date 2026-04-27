package com.anomalydetection.domain.projects;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "anomaly_detection_projects")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLRestriction("is_deleted = false")
public class AnomalyDetectionProject extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "project_code", nullable = false, length = 50)
  private String projectCode;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 2000)
  private String description;

  @Column(nullable = false, length = 32)
  private String status;

  @Column(name = "vehicle_model", length = 100)
  private String vehicleModel;

  @Column(name = "model_year", length = 10)
  private String modelYear;

  @Column(length = 100)
  private String platform;

  @Column(name = "primary_system", length = 100)
  private String primarySystem;

  @Column(name = "target_market", length = 100)
  private String targetMarket;

  @Column(name = "oem_code", length = 100)
  private String oemCode;

  @Column(name = "oem_name", length = 200)
  private String oemName;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "planned_end_date")
  private LocalDate plannedEndDate;

  @Column(name = "actual_end_date")
  private LocalDate actualEndDate;

  @Column(name = "progress_percentage", nullable = false)
  private int progressPercentage;

  @Column(name = "project_manager_id", columnDefinition = "BINARY(16)")
  private UUID projectManagerId;

  @Column(length = 16, nullable = false)
  private String priority;

  @Column(name = "is_confidential", nullable = false)
  private boolean isConfidential;

  @Column(length = 2000)
  private String notes;

  @Column(name = "total_detection_logics", nullable = false)
  private int totalDetectionLogics;

  @Column(name = "total_can_signals", nullable = false)
  private int totalCanSignals;

  @Column(name = "total_anomalies", nullable = false)
  private int totalAnomalies;

  @Column(name = "resolved_anomalies", nullable = false)
  private int resolvedAnomalies;

  protected AnomalyDetectionProject() {}

  public AnomalyDetectionProject(UUID id, String projectCode, String name, String status, String priority) {
    this.id = id;
    this.projectCode = projectCode;
    this.name = name;
    this.status = status;
    this.priority = priority;
  }

  @Override
  public UUID getId() {
    return id;
  }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getProjectCode() { return projectCode; }
  public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public String getVehicleModel() { return vehicleModel; }
  public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

  public String getModelYear() { return modelYear; }
  public void setModelYear(String modelYear) { this.modelYear = modelYear; }

  public String getPlatform() { return platform; }
  public void setPlatform(String platform) { this.platform = platform; }

  public String getPrimarySystem() { return primarySystem; }
  public void setPrimarySystem(String primarySystem) { this.primarySystem = primarySystem; }

  public String getTargetMarket() { return targetMarket; }
  public void setTargetMarket(String targetMarket) { this.targetMarket = targetMarket; }

  public String getOemCode() { return oemCode; }
  public void setOemCode(String oemCode) { this.oemCode = oemCode; }

  public String getOemName() { return oemName; }
  public void setOemName(String oemName) { this.oemName = oemName; }

  public LocalDate getStartDate() { return startDate; }
  public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

  public LocalDate getPlannedEndDate() { return plannedEndDate; }
  public void setPlannedEndDate(LocalDate plannedEndDate) { this.plannedEndDate = plannedEndDate; }

  public LocalDate getActualEndDate() { return actualEndDate; }
  public void setActualEndDate(LocalDate actualEndDate) { this.actualEndDate = actualEndDate; }

  public int getProgressPercentage() { return progressPercentage; }
  public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }

  public UUID getProjectManagerId() { return projectManagerId; }
  public void setProjectManagerId(UUID projectManagerId) { this.projectManagerId = projectManagerId; }

  public String getPriority() { return priority; }
  public void setPriority(String priority) { this.priority = priority; }

  public boolean isConfidential() { return isConfidential; }
  public void setConfidential(boolean isConfidential) { this.isConfidential = isConfidential; }

  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }

  public int getTotalDetectionLogics() { return totalDetectionLogics; }
  public void setTotalDetectionLogics(int v) { this.totalDetectionLogics = v; }

  public int getTotalCanSignals() { return totalCanSignals; }
  public void setTotalCanSignals(int v) { this.totalCanSignals = v; }

  public int getTotalAnomalies() { return totalAnomalies; }
  public void setTotalAnomalies(int v) { this.totalAnomalies = v; }

  public int getResolvedAnomalies() { return resolvedAnomalies; }
  public void setResolvedAnomalies(int v) { this.resolvedAnomalies = v; }
}
