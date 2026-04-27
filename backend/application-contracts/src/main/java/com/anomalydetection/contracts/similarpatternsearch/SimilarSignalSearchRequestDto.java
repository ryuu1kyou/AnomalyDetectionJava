package com.anomalydetection.contracts.similarpatternsearch;

import java.util.List;
import java.util.UUID;

public record SimilarSignalSearchRequestDto(
    UUID targetSignalId,
    List<UUID> candidateSignalIds,
    double minimumSimilarity,
    int maxResults,
    boolean compareFrameId,
    boolean compareSignalName,
    boolean compareLength) {}
