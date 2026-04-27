package com.anomalydetection.application.events;

import com.anomalydetection.domain.base.AggregateRoot;
import com.anomalydetection.domain.events.OutboxEvent;
import com.anomalydetection.domain.events.OutboxEventRepository;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Writes {@link AggregateRoot#localEvents()} into an outbox table.
 *
 * <p>This intentionally does not publish to a message broker. Publication is handled by
 * {@code OutboxPublisherJob}.
 */
@Component
public class DomainEventOutboxWriter {

  private final OutboxEventRepository repository;
  private final ObjectMapper objectMapper;
  private final ICurrentTenant currentTenant;

  public DomainEventOutboxWriter(
      OutboxEventRepository repository,
      ObjectMapper objectMapper,
      ICurrentTenant currentTenant) {
    this.repository = repository;
    this.objectMapper = objectMapper;
    this.currentTenant = currentTenant;
  }

  /**
   * Persists all local events collected in the aggregate.
   *
   * <p>Events are stored immediately, but clearing the local events is delayed until after commit.
   */
  public void writeFrom(AggregateRoot<?> aggregate) {
    if (aggregate == null) return;
    var events = aggregate.localEvents();
    if (events.isEmpty()) return;

    UUID tenantId = currentTenant.getTenantId().orElse(null);
    String aggregateType = aggregate.getClass().getName();
    String aggregateId = aggregate.getId() != null ? aggregate.getId().toString() : null;

    for (Object evt : events) {
      try {
        String payload = objectMapper.writeValueAsString(evt);
        var outbox = new OutboxEvent(
            UUID.randomUUID(),
            tenantId,
            aggregateType,
            aggregateId,
            evt.getClass().getName(),
            payload,
            Instant.now());
        repository.save(outbox);
      } catch (Exception e) {
        throw new IllegalStateException("Failed to serialize domain event: " + evt.getClass().getName(), e);
      }
    }

    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          aggregate.clearLocalEvents();
        }
      });
    } else {
      // Fallback: clear immediately.
      aggregate.clearLocalEvents();
    }
  }
}
