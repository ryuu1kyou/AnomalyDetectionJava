package com.anomalydetection.domain.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Abstract audited entity with soft-delete, matching ABP {@code IFullAuditedObject}.
 *
 * <p>Subclasses should use {@code @SQLDelete} and a Hibernate {@code @Where} or {@code @Filter}
 * to filter out soft-deleted rows.
 */
@MappedSuperclass
public abstract class FullAuditedEntity<ID extends Serializable> extends AggregateRoot<ID> {

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "last_modified_at")
  private Instant lastModifiedAt;

  @Column(name = "last_modified_by")
  private UUID lastModifiedBy;

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "deleted_by")
  private UUID deletedBy;

  // --- lifecycle hooks ---

  @jakarta.persistence.PrePersist
  protected void prePersist() {
    createdAt = Instant.now();
    if (createdBy == null) {
      createdBy = com.anomalydetection.shared.CurrentUserIdHolder.getUserId().orElse(null);
    }
  }

  @jakarta.persistence.PreUpdate
  protected void preUpdate() {
    lastModifiedAt = Instant.now();
    lastModifiedBy = com.anomalydetection.shared.CurrentUserIdHolder.getUserId().orElse(null);
  }

  // --- soft-delete support ---

  /** Soft-deletes this entity. Sets {@code isDeleted = true} and records the time. */
  public void softDelete(UUID byUserId) {
    this.isDeleted = true;
    this.deletedAt = Instant.now();
    this.deletedBy = byUserId;
  }

  public void restore() {
    this.isDeleted = false;
    this.deletedAt = null;
    this.deletedBy = null;
  }

  // --- getters (no public setters — use lifecycle hooks) ---

  public Instant getCreatedAt() {
    return createdAt;
  }

  public UUID getCreatedBy() {
    return createdBy;
  }

  public Instant getLastModifiedAt() {
    return lastModifiedAt;
  }

  public UUID getLastModifiedBy() {
    return lastModifiedBy;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public UUID getDeletedBy() {
    return deletedBy;
  }
}