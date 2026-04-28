package com.anomalydetection.domain.projects;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "project_milestones")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLDelete(sql = "UPDATE project_milestones SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class ProjectMilestone extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "project_id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID projectId;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 2000)
  private String description;

  @Column(name = "planned_date")
  private LocalDate plannedDate;

  @Column(name = "actual_date")
  private LocalDate actualDate;

  @Column(nullable = false, length = 32)
  private String status;

  @Column(name = "progress_percentage", nullable = false)
  private int progressPercentage;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  @Column(columnDefinition = "LONGTEXT")
  private String dependencies;

  @Column(columnDefinition = "LONGTEXT")
  private String deliverables;

  protected ProjectMilestone() {}

  public ProjectMilestone(UUID id, UUID projectId, String name) {
    this.id = id;
    this.projectId = projectId;
    this.name = name;
    this.status = "NotStarted";
    this.progressPercentage = 0;
    this.displayOrder = 0;
  }

  @Override
  public UUID getId() { return id; }

  public UUID getProjectId() { return projectId; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public LocalDate getPlannedDate() { return plannedDate; }
  public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }

  public LocalDate getActualDate() { return actualDate; }
  public void setActualDate(LocalDate actualDate) { this.actualDate = actualDate; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public int getProgressPercentage() { return progressPercentage; }
  public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }

  public int getDisplayOrder() { return displayOrder; }
  public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

  public String getDependencies() { return dependencies; }
  public void setDependencies(String dependencies) { this.dependencies = dependencies; }

  public String getDeliverables() { return deliverables; }
  public void setDeliverables(String deliverables) { this.deliverables = deliverables; }
}
