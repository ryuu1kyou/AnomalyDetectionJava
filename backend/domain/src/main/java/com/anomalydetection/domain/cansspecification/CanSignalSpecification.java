package com.anomalydetection.domain.cansspecification;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "can_signal_specifications")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLDelete(sql = "UPDATE can_signal_specifications SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class CanSignalSpecification extends FullAuditedEntity<UUID> {

  public enum ConversionType {
    LINEAR,
    LOOKUP_TABLE,
    FORMULA,
    RAW
  }

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "signal_identifier", nullable = false, length = 128)
  private String signalIdentifier;

  @Column(nullable = false, length = 256)
  private String name;

  @Column(name = "system_category_id", columnDefinition = "BINARY(16)")
  private UUID systemCategoryId;

  @Enumerated(EnumType.STRING)
  @Column(name = "conversion_type", nullable = false, length = 32)
  private ConversionType conversionType;

  @Column(name = "offset_value")
  private Double offset;

  @Column
  private Double gain;

  @Column(name = "min_value")
  private Double minValue;

  @Column(name = "max_value")
  private Double maxValue;

  @Column(length = 64)
  private String unit;

  @Column(columnDefinition = "TEXT")
  private String description;

  protected CanSignalSpecification() {}

  public CanSignalSpecification(UUID id, String signalIdentifier, String name, ConversionType conversionType) {
    this.id = id;
    this.signalIdentifier = signalIdentifier;
    this.name = name;
    this.conversionType = conversionType;
  }

  @Override
  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getSignalIdentifier() { return signalIdentifier; }
  public void setSignalIdentifier(String signalIdentifier) { this.signalIdentifier = signalIdentifier; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public UUID getSystemCategoryId() { return systemCategoryId; }
  public void setSystemCategoryId(UUID systemCategoryId) { this.systemCategoryId = systemCategoryId; }

  public ConversionType getConversionType() { return conversionType; }
  public void setConversionType(ConversionType conversionType) { this.conversionType = conversionType; }

  public Double getOffset() { return offset; }
  public void setOffset(Double offset) { this.offset = offset; }

  public Double getGain() { return gain; }
  public void setGain(Double gain) { this.gain = gain; }

  public Double getMinValue() { return minValue; }
  public void setMinValue(Double minValue) { this.minValue = minValue; }

  public Double getMaxValue() { return maxValue; }
  public void setMaxValue(Double maxValue) { this.maxValue = maxValue; }

  public String getUnit() { return unit; }
  public void setUnit(String unit) { this.unit = unit; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
}