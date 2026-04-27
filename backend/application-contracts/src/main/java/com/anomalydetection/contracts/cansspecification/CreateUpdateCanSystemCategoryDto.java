package com.anomalydetection.contracts.cansspecification;

public record CreateUpdateCanSystemCategoryDto(
    String name,
    String description,
    int displayOrder
) {}