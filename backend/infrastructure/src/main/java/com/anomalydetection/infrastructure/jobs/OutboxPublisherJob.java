package com.anomalydetection.infrastructure.jobs;

import com.anomalydetection.domain.events.OutboxEvent;
import com.anomalydetection.domain.events.OutboxEventRepository;
import java.time.Instant;
import java.util.List;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Profile;

/**
 * Publishes events stored in {@code outbox_events}.
 *
 * <p>Implementation strategy:
 * <ul>
 *   <li>We keep our own outbox table for domain events produced from {@link com.anomalydetection.domain.base.AggregateRoot#localEvents()}.</li>
 *   <li>We publish them as Spring application events, which Spring Modulith will persist to EVENT_PUBLICATION and deliver reliably.</li>
 * </ul>
 */
@Component
@Profile("!test")
public class OutboxPublisherJob {

  private static final Logger log = LoggerFactory.getLogger(OutboxPublisherJob.class);

  private final OutboxEventRepository outboxRepository;
  private final ApplicationEventPublisher eventPublisher;

  public OutboxPublisherJob(
      OutboxEventRepository outboxRepository,
      ApplicationEventPublisher eventPublisher) {
    this.outboxRepository = outboxRepository;
    this.eventPublisher = eventPublisher;
  }

  @Scheduled(fixedDelayString = "${app.jobs.outbox-publisher.delay:5000}")
  @SchedulerLock(name = "outboxPublisher", lockAtMostFor = "2m", lockAtLeastFor = "5s")
  @Transactional
  public void publishBatch() {
    List<OutboxEvent> batch = outboxRepository.findTop100ByPublishedAtIsNullOrderByOccurredAtAsc();
    if (batch.isEmpty()) return;

    for (OutboxEvent evt : batch) {
      try {
        // We publish the raw JSON payload as a dedicated externalized event.
        eventPublisher.publishEvent(new OutboxJsonPayloadEvent(
            evt.getId(),
            evt.getTenantId(),
            evt.getAggregateType(),
            evt.getAggregateId(),
            evt.getEventType(),
            evt.getPayload(),
            evt.getOccurredAt()
        ));

        evt.markPublished(Instant.now());
        outboxRepository.save(evt);
      } catch (Exception e) {
        log.warn("Failed to publish outbox event {}: {}", evt.getId(), e.getMessage());
        evt.markAttemptFailed(e.getClass().getName() + ": " + e.getMessage());
        outboxRepository.save(evt);
      }
    }
  }

  /**
   * Payload published into Spring Modulith for reliable delivery.
   */
  public record OutboxJsonPayloadEvent(
      java.util.UUID outboxId,
      java.util.UUID tenantId,
      String aggregateType,
      String aggregateId,
      String eventType,
      String payload,
      java.time.Instant occurredAt) {}
}
