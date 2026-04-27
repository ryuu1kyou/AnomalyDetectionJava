package com.anomalydetection.contracts.detectiontemplates;

import java.util.UUID;

public record CreateUpdateDetectionTemplateDto(
    String name,
    String description,
    UUID canSignalId,
    String expression,
    Double threshold,
    boolean isActive) {}
