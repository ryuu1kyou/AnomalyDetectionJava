package com.anomalydetection.domain.integration;

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
@Table(name = "integration_endpoints")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLRestriction("is_deleted = false")
public class IntegrationEndpoint extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private IntegrationType type;

  @Column(name = "base_url", nullable = false, length = 500)
  private String baseUrl;

  @Column(name = "endpoint_url", length = 500)
  private String endpointUrl;

  @Column(name = "api_key", length = 500)
  private String apiKey;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Column(nullable = false)
  private int timeout;

  @Column(name = "require_authentication", nullable = false)
  private boolean requireAuthentication;

  @Column(name = "authentication_scheme", length = 50)
  private String authenticationScheme;

  @Column(columnDefinition = "LONGTEXT")
  private String configuration;

  @Column(name = "last_sync_date")
  private Instant lastSyncDate;

  @Column(name = "success_count", nullable = false)
  private int successCount;

  @Column(name = "failure_count", nullable = false)
  private int failureCount;

  protected IntegrationEndpoint() {}

  public IntegrationEndpoint(UUID id, String name, IntegrationType type, String baseUrl,
      String description) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.baseUrl = baseUrl;
    this.endpointUrl = baseUrl;
    this.description = description;
    this.isActive = true;
    this.timeout = 30;
    this.successCount = 0;
    this.failureCount = 0;
  }

  public void recordSuccess() {
    this.successCount++;
    this.lastSyncDate = Instant.now();
  }

  public void recordFailure() {
    this.failureCount++;
  }

  @Override
  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public IntegrationType getType() { return type; }

  public String getBaseUrl() { return baseUrl; }
  public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

  public String getEndpointUrl() { return endpointUrl; }
  public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }

  public String getApiKey() { return apiKey; }
  public void setApiKey(String apiKey) { this.apiKey = apiKey; }

  public boolean isActive() { return isActive; }
  public void setActive(boolean active) { isActive = active; }

  public int getTimeout() { return timeout; }
  public void setTimeout(int timeout) { this.timeout = timeout; }

  public boolean isRequireAuthentication() { return requireAuthentication; }
  public void setRequireAuthentication(boolean requireAuthentication) {
    this.requireAuthentication = requireAuthentication;
  }

  public String getAuthenticationScheme() { return authenticationScheme; }
  public void setAuthenticationScheme(String authenticationScheme) {
    this.authenticationScheme = authenticationScheme;
  }

  public String getConfiguration() { return configuration; }
  public void setConfiguration(String configuration) { this.configuration = configuration; }

  public Instant getLastSyncDate() { return lastSyncDate; }
  public int getSuccessCount() { return successCount; }
  public int getFailureCount() { return failureCount; }
}
