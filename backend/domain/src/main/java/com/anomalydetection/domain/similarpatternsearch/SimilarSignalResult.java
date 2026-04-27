package com.anomalydetection.domain.similarpatternsearch;

import java.util.List;
import java.util.UUID;

public class SimilarSignalResult {

  private final UUID signalId;
  private final double similarityScore;
  private final double frameIdSimilarity;
  private final double nameSimilarity;
  private final double lengthSimilarity;
  private final List<String> matchedAttributes;
  private final List<String> differences;
  private final RecommendationLevel recommendationLevel;
  private final String recommendationReason;

  public SimilarSignalResult(
      UUID signalId,
      double similarityScore,
      double frameIdSimilarity,
      double nameSimilarity,
      double lengthSimilarity,
      List<String> matchedAttributes,
      List<String> differences,
      RecommendationLevel recommendationLevel,
      String recommendationReason) {
    this.signalId = signalId;
    this.similarityScore = similarityScore;
    this.frameIdSimilarity = frameIdSimilarity;
    this.nameSimilarity = nameSimilarity;
    this.lengthSimilarity = lengthSimilarity;
    this.matchedAttributes = List.copyOf(matchedAttributes);
    this.differences = List.copyOf(differences);
    this.recommendationLevel = recommendationLevel;
    this.recommendationReason = recommendationReason;
  }

  public UUID getSignalId() { return signalId; }
  public double getSimilarityScore() { return similarityScore; }
  public double getFrameIdSimilarity() { return frameIdSimilarity; }
  public double getNameSimilarity() { return nameSimilarity; }
  public double getLengthSimilarity() { return lengthSimilarity; }
  public List<String> getMatchedAttributes() { return matchedAttributes; }
  public List<String> getDifferences() { return differences; }
  public RecommendationLevel getRecommendationLevel() { return recommendationLevel; }
  public String getRecommendationReason() { return recommendationReason; }
}
