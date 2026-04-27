package com.anomalydetection.infrastructure.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "user_id", columnDefinition = "BINARY(16)")
  private UUID userId;

  @Column(name = "user_name", length = 256)
  private String userName;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "http_method", length = 16)
  private String httpMethod;

  @Column(length = 2048)
  private String url;

  @Column(name = "action_name", length = 256)
  private String actionName;

  @Column(name = "http_status_code")
  private Integer httpStatusCode;

  @Column(name = "execution_duration")
  private Long executionDuration;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(columnDefinition = "LONGTEXT")
  private String exceptions;

  protected AuditLogEntity() {}

  public AuditLogEntity(UUID userId, String userName, UUID tenantId,
      String httpMethod, String url, String actionName, Instant occurredAt) {
    this.id = UUID.randomUUID();
    this.userId = userId;
    this.userName = userName;
    this.tenantId = tenantId;
    this.httpMethod = httpMethod;
    this.url = url;
    this.actionName = actionName;
    this.occurredAt = occurredAt;
  }

  public UUID getId() { return id; }
  public void setHttpStatusCode(Integer code) { this.httpStatusCode = code; }
  public void setExecutionDuration(Long ms) { this.executionDuration = ms; }
  public void setExceptions(String ex) { this.exceptions = ex; }
}