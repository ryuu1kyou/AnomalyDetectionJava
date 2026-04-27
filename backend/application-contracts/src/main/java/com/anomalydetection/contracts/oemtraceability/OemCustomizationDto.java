package com.anomalydetection.contracts.oemtraceability;

import com.anomalydetection.domain.oemtraceability.OemCustomizationStatus;
import com.anomalydetection.domain.oemtraceability.OemCustomizationType;

public record OemCustomizationDto(
    String id,
    String entityId,
    String entityType,
    String oemCode,
    OemCustomizationType type,
    String customParameters,
    String originalParameters,
    String customizationReason,
    String approvedBy,
    String approvedAt,
    OemCustomizationStatus status,
    String approvalNotes) {}
