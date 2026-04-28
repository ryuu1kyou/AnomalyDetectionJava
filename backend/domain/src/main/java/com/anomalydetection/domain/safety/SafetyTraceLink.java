package com.anomalydetection.domain.safety;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "safety_trace_links")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLDelete(sql = "UPDATE safety_trace_links SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class SafetyTraceLink extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "source_record_id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID sourceRecordId;

  @Column(name = "target_record_id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID targetRecordId;

  @Column(name = "link_type", length = 100)
  private String linkType;

  @Column(length = 100)
  private String relation;

  @Column(columnDefinition = "LONGTEXT")
  private String history;

  protected SafetyTraceLink() {}

  public SafetyTraceLink(UUID id, UUID sourceRecordId, UUID targetRecordId,
      String linkType, String relation) {
    this.id = id;
    this.sourceRecordId = sourceRecordId;
    this.targetRecordId = targetRecordId;
    this.linkType = linkType;
    this.relation = relation;
  }

  public void update(String linkType, String relation) {
    this.linkType = linkType;
    this.relation = relation;
  }

  @Override
  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public UUID getSourceRecordId() { return sourceRecordId; }
  public UUID getTargetRecordId() { return targetRecordId; }

  public String getLinkType() { return linkType; }
  public void setLinkType(String linkType) { this.linkType = linkType; }

  public String getRelation() { return relation; }
  public void setRelation(String relation) { this.relation = relation; }

  public String getHistory() { return history; }
  public void setHistory(String history) { this.history = history; }
}
