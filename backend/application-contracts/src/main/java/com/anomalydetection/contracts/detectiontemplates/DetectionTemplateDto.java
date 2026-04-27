package com.anomalydetection.contracts.detectiontemplates;

import java.util.UUID;

public record DetectionTemplateDto(
    UUID id,
    UUID tenantId,
    String name,
    String description,
    UUID canSignalId,
    String expression,
    Double threshold,
    boolean isActive) {}
