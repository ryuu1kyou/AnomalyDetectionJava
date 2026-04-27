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
@Table(name = "project_members")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLRestriction("is_deleted = false")
public class ProjectMember extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "project_id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID projectId;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "user_id", columnDefinition = "BINARY(16)")
  private UUID userId;

  @Column(name = "user_name", length = 200)
  private String userName;

  @Column(length = 200)
  private String email;

  @Column(length = 50)
  private String role;

  @Column(columnDefinition = "LONGTEXT")
  private String responsibilities;

  @Column(name = "joined_date")
  private LocalDate joinedDate;

  @Column(name = "left_date")
  private LocalDate leftDate;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Column(name = "can_edit", nullable = false)
  private boolean canEdit;

  @Column(name = "can_delete", nullable = false)
  private boolean canDelete;

  @Column(name = "can_manage_members", nullable = false)
  private boolean canManageMembers;

  protected ProjectMember() {}

  public ProjectMember(UUID id, UUID projectId, UUID userId, String role) {
    this.id = id;
    this.projectId = projectId;
    this.userId = userId;
    this.role = role;
    this.isActive = true;
    this.joinedDate = LocalDate.now();
  }

  @Override
  public UUID getId() { return id; }

  public UUID getProjectId() { return projectId; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public UUID getUserId() { return userId; }

  public String getUserName() { return userName; }
  public void setUserName(String userName) { this.userName = userName; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }

  public String getResponsibilities() { return responsibilities; }
  public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }

  public LocalDate getJoinedDate() { return joinedDate; }
  public void setJoinedDate(LocalDate joinedDate) { this.joinedDate = joinedDate; }

  public LocalDate getLeftDate() { return leftDate; }
  public void setLeftDate(LocalDate leftDate) { this.leftDate = leftDate; }

  public boolean isActive() { return isActive; }
  public void setActive(boolean isActive) { this.isActive = isActive; }

  public boolean isCanEdit() { return canEdit; }
  public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }

  public boolean isCanDelete() { return canDelete; }
  public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }

  public boolean isCanManageMembers() { return canManageMembers; }
  public void setCanManageMembers(boolean canManageMembers) { this.canManageMembers = canManageMembers; }
}
