package com.anomalydetection.infrastructure.events;

import com.anomalydetection.domain.events.OutboxEvent;
import com.anomalydetection.domain.events.OutboxEventRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOutboxEventRepository extends JpaRepository<OutboxEvent, UUID>, OutboxEventRepository {

  @Override
  List<OutboxEvent> findTop100ByPublishedAtIsNullOrderByOccurredAtAsc();

  @Override
  long deleteByPublishedAtBefore(Instant cutoff);
}
