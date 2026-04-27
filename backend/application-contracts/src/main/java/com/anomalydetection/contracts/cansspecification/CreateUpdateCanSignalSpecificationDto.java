package com.anomalydetection.contracts.cansspecification;

import java.util.UUID;

public record CreateUpdateCanSignalSpecificationDto(
    String signalIdentifier,
    String name,
    UUID systemCategoryId,
    String conversionType,
    Double offset,
    Double gain,
    Double minValue,
    Double maxValue,
    String unit,
    String description
) {}