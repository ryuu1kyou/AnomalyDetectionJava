package com.anomalydetection.contracts.integration;

public record IntegrationLogDto(
    String id,
    String endpointId,
    String timestamp,
    String level,
    String operation,
    boolean success,
    String errorMessage,
    Integer statusCode,
    Long durationMs) {}
