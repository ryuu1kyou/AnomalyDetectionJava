package com.anomalydetection.contracts.oemtraceability;

import com.anomalydetection.domain.oemtraceability.OemCustomizationType;

public record CreateOemCustomizationDto(
    String entityId,
    String entityType,
    String oemCode,
    OemCustomizationType type,
    String customParameters,
    String originalParameters,
    String customizationReason) {}
