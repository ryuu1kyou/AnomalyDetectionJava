package com.anomalydetection.contracts.integration;

import java.util.UUID;

public record CreateDataImportRequestDto(
    UUID endpointId,
    String dataType,
    String filter) {}
