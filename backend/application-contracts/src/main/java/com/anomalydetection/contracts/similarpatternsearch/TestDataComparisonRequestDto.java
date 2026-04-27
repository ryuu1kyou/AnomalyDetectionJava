package com.anomalydetection.contracts.similarpatternsearch;

import java.util.UUID;

public record TestDataComparisonRequestDto(
    UUID sourceSignalId,
    UUID targetSignalId,
    int maxResults) {}
