package com.anomalydetection.application.similarpatternsearch;

import com.anomalydetection.contracts.similarpatternsearch.SimilarSignalResultDto;
import com.anomalydetection.contracts.similarpatternsearch.SimilarSignalSearchRequestDto;
import com.anomalydetection.contracts.similarpatternsearch.TestDataComparisonDto;
import com.anomalydetection.contracts.similarpatternsearch.TestDataComparisonRequestDto;
import com.anomalydetection.domain.anomalydetection.AnomalyDetectionResult;
import com.anomalydetection.domain.anomalydetection.AnomalyDetectionResultRepository;
import com.anomalydetection.domain.anomalydetection.AnomalyLevel;
import com.anomalydetection.domain.cansignals.CanSignal;
import com.anomalydetection.domain.cansignals.CanSignalRepository;
import com.anomalydetection.domain.similarpatternsearch.SimilarPatternSearchService;
import com.anomalydetection.domain.similarpatternsearch.SimilarSignalResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SimilarPatternSearchAppService {

  private final SimilarPatternSearchService searchService;
  private final CanSignalRepository canSignalRepo;
  private final AnomalyDetectionResultRepository resultRepo;

  public SimilarPatternSearchAppService(
      SimilarPatternSearchService searchService,
      CanSignalRepository canSignalRepo,
      AnomalyDetectionResultRepository resultRepo) {
    this.searchService = searchService;
    this.canSignalRepo = canSignalRepo;
    this.resultRepo = resultRepo;
  }

  public List<SimilarSignalResultDto> searchSimilarSignals(SimilarSignalSearchRequestDto request) {
    var target = canSignalRepo.findById(request.targetSignalId()).orElseThrow(
        () -> new IllegalArgumentException("Signal not found: " + request.targetSignalId()));

    List<CanSignal> candidates;
    if (request.candidateSignalIds() != null && !request.candidateSignalIds().isEmpty()) {
      candidates = request.candidateSignalIds().stream()
          .map(id -> canSignalRepo.findById(id))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .toList();
    } else {
      candidates = canSignalRepo.findAll();
    }

    double minSim = request.minimumSimilarity() > 0 ? request.minimumSimilarity() : 0.5;
    int maxResults = request.maxResults() > 0 ? request.maxResults() : 50;
    boolean compareFrameId = request.compareFrameId();
    boolean compareName = request.compareSignalName();
    boolean compareLength = request.compareLength();

    if (!compareFrameId && !compareName && !compareLength) {
      compareFrameId = true;
      compareName = true;
    }

    var results = searchService.searchSimilarSignals(
        target, candidates, minSim, maxResults, compareFrameId, compareName, compareLength);

    return results.stream()
        .map(r -> toDto(r, canSignalRepo.findById(r.getSignalId()).orElse(null)))
        .filter(d -> d != null)
        .toList();
  }

  public TestDataComparisonDto compareTestData(TestDataComparisonRequestDto request) {
    int maxResults = request.maxResults() > 0 ? request.maxResults() : 1000;

    var sourceResults = resultRepo.findAllByCanSignalId(request.sourceSignalId());
    var targetResults = resultRepo.findAllByCanSignalId(request.targetSignalId());

    if (sourceResults.size() > maxResults) sourceResults = sourceResults.subList(0, maxResults);
    if (targetResults.size() > maxResults) targetResults = targetResults.subList(0, maxResults);

    var thresholdDiffs = analyzeAnomalyLevelDifferences(sourceResults, targetResults);
    var conditionDiffs = analyzeConditionDifferences(sourceResults, targetResults);
    var resultDiffs = analyzeConfidenceDifferences(sourceResults, targetResults);

    int significantDiffs = thresholdDiffs.size() + conditionDiffs.size() + resultDiffs.size();
    double similarity = significantDiffs == 0 ? 1.0 : Math.max(0.0, 1.0 - (double) significantDiffs / 10.0);

    var recommendations = new ArrayList<String>();
    if (!thresholdDiffs.isEmpty())
      recommendations.add("Review and adjust threshold parameters based on significant differences");
    if (!conditionDiffs.isEmpty())
      recommendations.add("Consider updating detection conditions to align with target signal");

    String summary = String.format(
        "Source: %d results, Target: %d results. Differences - Thresholds: %d, Conditions: %d, Results: %d. Overall Similarity: %.1f%%",
        sourceResults.size(), targetResults.size(),
        thresholdDiffs.size(), conditionDiffs.size(), resultDiffs.size(), similarity * 100);

    return new TestDataComparisonDto(
        request.sourceSignalId().toString(),
        request.targetSignalId().toString(),
        similarity,
        summary,
        sourceResults.size(),
        targetResults.size(),
        thresholdDiffs,
        conditionDiffs,
        resultDiffs,
        recommendations);
  }

  public List<SimilarSignalResultDto> getRecommendations(UUID signalId, int maxRecommendations) {
    var request = new SimilarSignalSearchRequestDto(
        signalId, null, 0.5, maxRecommendations * 2, true, true, false);
    return searchSimilarSignals(request).stream().limit(maxRecommendations).toList();
  }

  private List<String> analyzeAnomalyLevelDifferences(
      List<AnomalyDetectionResult> source, List<AnomalyDetectionResult> target) {
    var diffs = new ArrayList<String>();
    var sourceCounts = source.stream()
        .collect(Collectors.groupingBy(AnomalyDetectionResult::getAnomalyLevel, Collectors.counting()));
    var targetCounts = target.stream()
        .collect(Collectors.groupingBy(AnomalyDetectionResult::getAnomalyLevel, Collectors.counting()));

    for (AnomalyLevel level : AnomalyLevel.values()) {
      long s = sourceCounts.getOrDefault(level, 0L);
      long t = targetCounts.getOrDefault(level, 0L);
      if (s != t) diffs.add(level + " count: " + s + " vs " + t);
    }
    return diffs;
  }

  private List<String> analyzeConditionDifferences(
      List<AnomalyDetectionResult> source, List<AnomalyDetectionResult> target) {
    var diffs = new ArrayList<String>();
    var sourceConditions = source.stream()
        .map(AnomalyDetectionResult::getTriggerCondition)
        .filter(c -> c != null)
        .collect(Collectors.toSet());
    var targetConditions = target.stream()
        .map(AnomalyDetectionResult::getTriggerCondition)
        .filter(c -> c != null)
        .collect(Collectors.toSet());

    sourceConditions.stream()
        .filter(c -> !targetConditions.contains(c))
        .forEach(c -> diffs.add("Only in source: " + c));
    targetConditions.stream()
        .filter(c -> !sourceConditions.contains(c))
        .forEach(c -> diffs.add("Only in target: " + c));
    return diffs;
  }

  private List<String> analyzeConfidenceDifferences(
      List<AnomalyDetectionResult> source, List<AnomalyDetectionResult> target) {
    var diffs = new ArrayList<String>();
    if (source.isEmpty() || target.isEmpty()) return diffs;

    double srcAvg = source.stream().mapToDouble(AnomalyDetectionResult::getConfidenceScore).average().orElse(0);
    double tgtAvg = target.stream().mapToDouble(AnomalyDetectionResult::getConfidenceScore).average().orElse(0);

    if (Math.abs(srcAvg - tgtAvg) > 0.1)
      diffs.add(String.format("Average confidence: %.2f vs %.2f", srcAvg, tgtAvg));
    return diffs;
  }

  private SimilarSignalResultDto toDto(SimilarSignalResult result, CanSignal signal) {
    if (signal == null) return null;
    return new SimilarSignalResultDto(
        result.getSignalId().toString(),
        signal.getName(),
        result.getSimilarityScore(),
        result.getFrameIdSimilarity(),
        result.getNameSimilarity(),
        result.getLengthSimilarity(),
        result.getMatchedAttributes(),
        result.getDifferences(),
        result.getRecommendationLevel(),
        result.getRecommendationReason());
  }
}
