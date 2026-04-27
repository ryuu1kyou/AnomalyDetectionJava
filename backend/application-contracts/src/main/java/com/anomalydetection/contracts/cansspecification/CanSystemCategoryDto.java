package com.anomalydetection.contracts.cansspecification;

import java.util.UUID;

public record CanSystemCategoryDto(
    UUID id,
    UUID tenantId,
    String name,
    String description,
    int displayOrder
) {}