package com.anomalydetection.domain.integration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "integration_logs")
public class IntegrationLog {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "endpoint_id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID endpointId;

  @Column(nullable = false)
  private Instant timestamp;

  @Column(nullable = false, length = 10)
  private String level;

  @Column(nullable = false, length = 200)
  private String operation;

  @Column(nullable = false)
  private boolean success;

  @Column(name = "request_data", columnDefinition = "LONGTEXT")
  private String requestData;

  @Column(name = "response_data", columnDefinition = "LONGTEXT")
  private String responseData;

  @Column(name = "error_message", columnDefinition = "LONGTEXT")
  private String errorMessage;

  @Column(name = "status_code")
  private Integer statusCode;

  @Column(name = "duration_ms")
  private Long durationMs;

  protected IntegrationLog() {}

  public IntegrationLog(UUID id, UUID endpointId, String operation, boolean success) {
    this.id = id;
    this.endpointId = endpointId;
    this.operation = operation;
    this.success = success;
    this.timestamp = Instant.now();
    this.level = success ? "INFO" : "ERROR";
  }

  public UUID getId() { return id; }
  public UUID getEndpointId() { return endpointId; }
  public Instant getTimestamp() { return timestamp; }
  public String getLevel() { return level; }
  public String getOperation() { return operation; }
  public boolean isSuccess() { return success; }
  public String getRequestData() { return requestData; }
  public void setRequestData(String requestData) { this.requestData = requestData; }
  public String getResponseData() { return responseData; }
  public void setResponseData(String responseData) { this.responseData = responseData; }
  public String getErrorMessage() { return errorMessage; }
  public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
  public Integer getStatusCode() { return statusCode; }
  public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
  public Long getDurationMs() { return durationMs; }
  public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
}
