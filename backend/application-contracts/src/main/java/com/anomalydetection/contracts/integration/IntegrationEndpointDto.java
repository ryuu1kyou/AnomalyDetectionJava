package com.anomalydetection.contracts.integration;

import com.anomalydetection.domain.integration.IntegrationType;

public record IntegrationEndpointDto(
    String id,
    String name,
    String description,
    IntegrationType type,
    String baseUrl,
    boolean isActive,
    int timeout,
    String lastSyncDate,
    int successCount,
    int failureCount,
    String createdAt,
    String lastModifiedAt) {}
