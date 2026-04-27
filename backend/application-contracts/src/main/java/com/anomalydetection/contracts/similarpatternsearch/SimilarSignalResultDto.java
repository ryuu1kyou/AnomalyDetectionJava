package com.anomalydetection.contracts.similarpatternsearch;

import com.anomalydetection.domain.similarpatternsearch.RecommendationLevel;
import java.util.List;

public record SimilarSignalResultDto(
    String signalId,
    String signalName,
    double similarityScore,
    double frameIdSimilarity,
    double nameSimilarity,
    double lengthSimilarity,
    List<String> matchedAttributes,
    List<String> differences,
    RecommendationLevel recommendationLevel,
    String recommendationReason) {}
