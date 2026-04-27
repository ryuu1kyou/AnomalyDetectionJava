package com.anomalydetection.domain.similarpatternsearch;

import com.anomalydetection.domain.cansignals.CanSignal;
import java.util.ArrayList;
import java.util.List;

public class SimilarPatternSearchService {

  public List<SimilarSignalResult> searchSimilarSignals(
      CanSignal target,
      List<CanSignal> candidates,
      double minSimilarity,
      int maxResults,
      boolean compareFrameId,
      boolean compareName,
      boolean compareLength) {

    var results = new ArrayList<SimilarSignalResult>();

    for (var candidate : candidates) {
      if (candidate.getId().equals(target.getId())) continue;

      double frameIdSim = compareFrameId ? (target.getFrameId() == candidate.getFrameId() ? 1.0 : 0.0) : 0.0;
      double nameSim = compareName ? calculateStringSimilarity(target.getName(), candidate.getName()) : 0.0;
      double lengthSim = compareLength ? calculateLengthSimilarity(target.getLength(), candidate.getLength()) : 0.0;

      double score = calculateWeightedScore(frameIdSim, nameSim, lengthSim, compareFrameId, compareName, compareLength);
      if (score < minSimilarity) continue;

      var matched = new ArrayList<String>();
      var differences = new ArrayList<String>();

      if (compareFrameId) {
        if (frameIdSim >= 1.0) matched.add("Frame ID");
        else differences.add("Frame ID: " + target.getFrameId() + " vs " + candidate.getFrameId());
      }
      if (compareName) {
        if (nameSim >= 0.8) matched.add("Signal Name");
        else differences.add("Signal Name: " + target.getName() + " vs " + candidate.getName());
      }
      if (compareLength) {
        if (lengthSim >= 0.9) matched.add("Length");
        else differences.add("Length: " + target.getLength() + " vs " + candidate.getLength());
      }

      results.add(new SimilarSignalResult(
          candidate.getId(),
          score,
          frameIdSim,
          nameSim,
          lengthSim,
          matched,
          differences,
          determineRecommendationLevel(score, differences.size()),
          generateRecommendationReason(score, differences.size())));
    }

    return results.stream()
        .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
        .limit(maxResults)
        .toList();
  }

  private double calculateWeightedScore(
      double frameIdSim, double nameSim, double lengthSim,
      boolean compareFrameId, boolean compareName, boolean compareLength) {
    double totalWeight = 0;
    double weightedSum = 0;

    if (compareFrameId) { weightedSum += frameIdSim * 0.4; totalWeight += 0.4; }
    if (compareName) { weightedSum += nameSim * 0.4; totalWeight += 0.4; }
    if (compareLength) { weightedSum += lengthSim * 0.2; totalWeight += 0.2; }

    return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
  }

  private double calculateStringSimilarity(String s1, String s2) {
    if (s1 == null || s2 == null) return 0.0;
    if (s1.equalsIgnoreCase(s2)) return 1.0;
    var l1 = s1.toLowerCase();
    var l2 = s2.toLowerCase();
    int distance = levenshteinDistance(l1, l2);
    int maxLen = Math.max(l1.length(), l2.length());
    return maxLen == 0 ? 1.0 : 1.0 - (double) distance / maxLen;
  }

  private double calculateLengthSimilarity(int l1, int l2) {
    if (l1 == l2) return 1.0;
    int diff = Math.abs(l1 - l2);
    int max = Math.max(l1, l2);
    return max == 0 ? 1.0 : 1.0 - (double) diff / max;
  }

  private int levenshteinDistance(String s1, String s2) {
    int[][] dp = new int[s1.length() + 1][s2.length() + 1];
    for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
    for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
    for (int i = 1; i <= s1.length(); i++) {
      for (int j = 1; j <= s2.length(); j++) {
        int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
        dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
      }
    }
    return dp[s1.length()][s2.length()];
  }

  private RecommendationLevel determineRecommendationLevel(double score, int diffCount) {
    if (score >= 0.95 && diffCount == 0) return RecommendationLevel.Highly;
    if (score >= 0.85 && diffCount <= 1) return RecommendationLevel.High;
    if (score >= 0.70 && diffCount <= 2) return RecommendationLevel.Medium;
    if (score >= 0.50 && diffCount <= 3) return RecommendationLevel.Low;
    return RecommendationLevel.NotRecommended;
  }

  private String generateRecommendationReason(double score, int diffCount) {
    if (score >= 0.9) return "High similarity with minimal differences - strongly recommended for reference";
    if (score >= 0.7 && diffCount <= 1) return "Good similarity with acceptable differences - recommended for reference";
    if (score >= 0.5) return "Moderate similarity - may be useful for reference with careful consideration";
    return "Low similarity - use with caution and thorough review";
  }
}
