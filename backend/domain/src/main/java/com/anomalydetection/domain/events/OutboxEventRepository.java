package com.anomalydetection.domain.events;

import com.anomalydetection.domain.base.BaseRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends BaseRepository<OutboxEvent, UUID> {

  /** Returns events that have not been published yet. */
  List<OutboxEvent> findTop100ByPublishedAtIsNullOrderByOccurredAtAsc();

  long deleteByPublishedAtBefore(Instant cutoff);
}
