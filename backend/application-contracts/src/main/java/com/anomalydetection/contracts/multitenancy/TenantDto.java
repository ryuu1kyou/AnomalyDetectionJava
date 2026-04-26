package com.anomalydetection.contracts.multitenancy;

import java.util.UUID;

public record TenantDto(
    UUID id,
    String name,
    boolean isActive) {}
