package com.anomalydetection.contracts.similarpatternsearch;

import java.util.List;

public record TestDataComparisonDto(
    String sourceSignalId,
    String targetSignalId,
    double overallSimilarityScore,
    String summary,
    int sourceResultCount,
    int targetResultCount,
    List<String> thresholdDifferences,
    List<String> conditionDifferences,
    List<String> resultDifferences,
    List<String> recommendations) {}
