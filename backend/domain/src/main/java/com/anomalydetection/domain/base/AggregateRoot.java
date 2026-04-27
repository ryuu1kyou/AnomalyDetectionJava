package com.anomalydetection.domain.base;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for aggregate roots — matches ABP {@code AggregateRoot<TKey>}.
 *
 * <p>Accumulates domain events in-memory. Events are typically flushed after the aggregate is
 * persisted (triggered by a {@code @PostPersist}/ {@code @PostUpdate} hook or an application
 * service that explicitly publishes them via Spring Modulith).
 */
@MappedSuperclass
public abstract class AggregateRoot<ID extends Serializable> extends Entity<ID> {

  @Transient
  private final List<Object> localEvents = new ArrayList<>();

  /** Registers an event to be published after the transaction commits. */
  protected void registerEvent(Object event) {
    if (event == null) {
      throw new IllegalArgumentException("event must not be null");
    }
    localEvents.add(event);
  }

  /** Returns an unmodifiable snapshot of the events collected so far. */
  public List<Object> localEvents() {
    return Collections.unmodifiableList(localEvents);
  }

  /** Discards all collected events (should be called after publication). */
  public void clearLocalEvents() {
    localEvents.clear();
  }
}