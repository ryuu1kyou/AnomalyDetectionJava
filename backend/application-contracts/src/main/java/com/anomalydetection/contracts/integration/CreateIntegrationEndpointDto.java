package com.anomalydetection.contracts.integration;

import com.anomalydetection.domain.integration.IntegrationType;

public record CreateIntegrationEndpointDto(
    String name,
    String description,
    IntegrationType type,
    String baseUrl,
    Boolean isActive,
    Integer timeout) {}
