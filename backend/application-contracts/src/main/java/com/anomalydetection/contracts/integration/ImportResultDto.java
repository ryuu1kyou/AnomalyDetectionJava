package com.anomalydetection.contracts.integration;

public record ImportResultDto(boolean success, int recordsImported, String message) {}
