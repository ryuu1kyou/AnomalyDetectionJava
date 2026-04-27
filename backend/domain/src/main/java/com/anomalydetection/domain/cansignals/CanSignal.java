package com.anomalydetection.domain.cansignals;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "can_signals")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLRestriction("is_deleted = false")
public class CanSignal extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "frame_id", nullable = false)
  private int frameId;

  @Column(nullable = false, length = 256)
  private String name;

  @Column(length = 512)
  private String description;

  @Column(name = "start_bit")
  private int startBit;

  @Column
  private int length;

  @Column(name = "byte_order", length = 16)
  private String byteOrder;

  @Column(name = "is_signed")
  private boolean isSigned;

  @Column(name = "specification_id", columnDefinition = "BINARY(16)")
  private UUID specificationId;

  protected CanSignal() {}

  public CanSignal(UUID id, int frameId, String name, int startBit, int length) {
    this.id = id;
    this.frameId = frameId;
    this.name = name;
    this.startBit = startBit;
    this.length = length;
  }

  @Override
  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public int getFrameId() { return frameId; }
  public void setFrameId(int frameId) { this.frameId = frameId; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public int getStartBit() { return startBit; }
  public void setStartBit(int startBit) { this.startBit = startBit; }

  public int getLength() { return length; }
  public void setLength(int length) { this.length = length; }

  public String getByteOrder() { return byteOrder; }
  public void setByteOrder(String byteOrder) { this.byteOrder = byteOrder; }

  public boolean isSigned() { return isSigned; }
  public void setSigned(boolean signed) { isSigned = signed; }

  public UUID getSpecificationId() { return specificationId; }
  public void setSpecificationId(UUID specificationId) { this.specificationId = specificationId; }
}