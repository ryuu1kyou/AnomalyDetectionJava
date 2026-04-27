package com.anomalydetection.domain.base;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.io.Serializable;

/**
 * Base entity with ID-based {@code equals}/{@code hashCode}, matching ABP {@code Entity<TKey>}.
 *
 * <p>Subclasses declare the actual {@code @Id} column. Two entities are equal when they share
 * the same type and non-null ID.
 */
@MappedSuperclass
public abstract class Entity<ID extends Serializable> {

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Entity<?> entity = (Entity<?>) o;
    ID myId = getId();
    Object otherId = entity.getId();
    if (myId == null || otherId == null) return false;
    return myId.equals(otherId);
  }

  @Override
  public int hashCode() {
    ID myId = getId();
    return myId != null ? myId.hashCode() : super.hashCode();
  }

  @Override
  public String toString() {
    ID myId = getId();
    var simpleName = getClass().getSimpleName();
    return myId != null ? simpleName + "(id=" + myId + ")" : simpleName + "(transient)";
  }

  /** Returns the primary key. May be {@code null} when not yet persisted. */
  @Transient
  public abstract ID getId();
}