package com.anomalydetection.contracts.integration;

import com.anomalydetection.domain.integration.ImportStatus;

public record DataImportRequestDto(
    String id,
    String endpointId,
    String dataType,
    ImportStatus status,
    String filter,
    String requestedAt,
    String processedDate,
    int recordsImported,
    String errorMessage) {}
