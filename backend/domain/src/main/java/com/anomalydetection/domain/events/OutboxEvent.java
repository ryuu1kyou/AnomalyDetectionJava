package com.anomalydetection.domain.events;

import com.anomalydetection.domain.base.AggregateRoot;
import com.anomalydetection.domain.base.MultiTenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Generic outbox event.
 *
 * <p>Written in the same DB transaction as the domain change, then published asynchronously
 * by a background job.
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent extends AggregateRoot<UUID> implements MultiTenant {

  @Id
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "aggregate_type", nullable = false, length = 512)
  private String aggregateType;

  @Column(name = "aggregate_id", length = 128)
  private String aggregateId;

  @Column(name = "event_type", nullable = false, length = 512)
  private String eventType;

  @Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT")
  private String payload;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(name = "attempts", nullable = false)
  private int attempts;

  @Column(name = "last_error", columnDefinition = "LONGTEXT")
  private String lastError;

  protected OutboxEvent() {}

  public OutboxEvent(
      UUID id,
      UUID tenantId,
      String aggregateType,
      String aggregateId,
      String eventType,
      String payload,
      Instant occurredAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    this.eventType = eventType;
    this.payload = payload;
    this.occurredAt = occurredAt;
    this.attempts = 0;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public UUID getTenantId() {
    return tenantId;
  }

  @Override
  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public String getAggregateId() {
    return aggregateId;
  }

  public String getEventType() {
    return eventType;
  }

  public String getPayload() {
    return payload;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public int getAttempts() {
    return attempts;
  }

  public String getLastError() {
    return lastError;
  }

  public void markPublished(Instant at) {
    this.publishedAt = at;
  }

  public void markAttemptFailed(String error) {
    this.attempts++;
    this.lastError = error;
  }
}
