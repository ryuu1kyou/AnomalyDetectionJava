package com.anomalydetection.infrastructure.features;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "feature_values")
public class FeatureValueEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 128)
  private String name;

  @Column(nullable = false, length = 512)
  private String value;

  @Column(name = "provider_name", nullable = false, length = 64)
  private String providerName;

  @Column(name = "provider_key", length = 64)
  private String providerKey;

  protected FeatureValueEntity() {}

  public FeatureValueEntity(String name, String value, String providerName, String providerKey) {
    this.name = name;
    this.value = value;
    this.providerName = providerName;
    this.providerKey = providerKey;
  }

  public Long getId() { return id; }
  public String getName() { return name; }
  public String getValue() { return value; }
  public String getProviderName() { return providerName; }
  public String getProviderKey() { return providerKey; }
}