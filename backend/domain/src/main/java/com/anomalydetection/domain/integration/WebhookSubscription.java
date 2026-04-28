package com.anomalydetection.domain.integration;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "webhook_subscriptions")
@SQLDelete(sql = "UPDATE webhook_subscriptions SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class WebhookSubscription extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "endpoint_id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID endpointId;

  @Column(name = "target_url", nullable = false, length = 500)
  private String targetUrl;

  @Column(name = "event_type", nullable = false, length = 100)
  private String eventType;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Column(length = 500)
  private String secret;

  @Column(name = "max_retries", nullable = false)
  private int maxRetries;

  @Column(name = "timeout_seconds", nullable = false)
  private int timeoutSeconds;

  @Column(name = "last_triggered_at")
  private Instant lastTriggeredAt;

  @Column(name = "delivery_success_count", nullable = false)
  private int deliverySuccessCount;

  @Column(name = "delivery_failure_count", nullable = false)
  private int deliveryFailureCount;

  protected WebhookSubscription() {}

  public WebhookSubscription(UUID id, UUID endpointId, String eventType, String targetUrl,
      boolean isActive) {
    this.id = id;
    this.endpointId = endpointId;
    this.eventType = eventType;
    this.targetUrl = targetUrl;
    this.isActive = isActive;
    this.maxRetries = 3;
    this.timeoutSeconds = 30;
    this.deliverySuccessCount = 0;
    this.deliveryFailureCount = 0;
  }

  public void recordSuccess() {
    this.deliverySuccessCount++;
    this.lastTriggeredAt = Instant.now();
  }

  public void recordFailure() {
    this.deliveryFailureCount++;
  }

  @Override
  public UUID getId() { return id; }

  public UUID getEndpointId() { return endpointId; }
  public String getTargetUrl() { return targetUrl; }
  public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

  public String getEventType() { return eventType; }
  public void setEventType(String eventType) { this.eventType = eventType; }

  public boolean isActive() { return isActive; }
  public void setActive(boolean active) { isActive = active; }

  public String getSecret() { return secret; }
  public void setSecret(String secret) { this.secret = secret; }

  public int getMaxRetries() { return maxRetries; }
  public int getTimeoutSeconds() { return timeoutSeconds; }
  public Instant getLastTriggeredAt() { return lastTriggeredAt; }
  public int getDeliverySuccessCount() { return deliverySuccessCount; }
  public int getDeliveryFailureCount() { return deliveryFailureCount; }
}
