package com.anomalydetection.domain.similarpatternsearch;

import static org.assertj.core.api.Assertions.assertThat;

import com.anomalydetection.domain.cansignals.CanSignal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SimilarPatternSearchServiceTest {

  private final SimilarPatternSearchService service = new SimilarPatternSearchService();

  private CanSignal signal(String name, int frameId, int length) {
    return new CanSignal(UUID.randomUUID(), frameId, name, 0, length);
  }

  @Test
  void identicalNameAndFrameIdScoresOne() {
    var target = signal("EngineSpeed", 0x100, 16);
    var candidate = signal("EngineSpeed", 0x100, 16);

    var results = service.searchSimilarSignals(target, List.of(candidate), 0.0, 10, true, true, true);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getSimilarityScore()).isEqualTo(1.0);
    assertThat(results.get(0).getRecommendationLevel()).isEqualTo(RecommendationLevel.Highly);
  }

  @Test
  void targetIsExcludedFromCandidates() {
    var target = signal("EngineSpeed", 0x100, 16);

    var results = service.searchSimilarSignals(target, List.of(target), 0.0, 10, true, true, false);

    assertThat(results).isEmpty();
  }

  @Test
  void minSimilarityFiltersLowScores() {
    var target = signal("EngineSpeed", 0x100, 16);
    var unrelated = signal("BrakeTemp", 0x200, 8);

    var results = service.searchSimilarSignals(target, List.of(unrelated), 0.9, 10, true, true, false);

    assertThat(results).isEmpty();
  }

  @Test
  void maxResultsLimitsOutput() {
    var target = signal("EngineSpeed", 0x100, 16);
    var c1 = signal("EngineSpeed_1", 0x100, 16);
    var c2 = signal("EngineSpeed_2", 0x100, 16);
    var c3 = signal("EngineSpeed_3", 0x100, 16);

    var results = service.searchSimilarSignals(target, List.of(c1, c2, c3), 0.0, 2, false, true, false);

    assertThat(results).hasSize(2);
  }

  @Test
  void resultsSortedByScoreDescending() {
    var target = signal("EngineSpeed", 0x100, 16);
    var highSim = signal("EngineSpeed", 0x100, 16);
    var lowSim = signal("Completely_Different_Name", 0x999, 64);

    var results = service.searchSimilarSignals(
        target, List.of(lowSim, highSim), 0.0, 10, true, true, true);

    assertThat(results.get(0).getSimilarityScore())
        .isGreaterThanOrEqualTo(results.get(1).getSimilarityScore());
  }

  @Test
  void frameIdOnlyComparisonWorksByExactMatch() {
    var target = signal("X", 0x100, 8);
    var sameFrame = signal("Y", 0x100, 32);
    var diffFrame = signal("Z", 0x200, 8);

    var results = service.searchSimilarSignals(
        target, List.of(sameFrame, diffFrame), 0.0, 10, true, false, false);

    assertThat(results).hasSize(2);
    var sameFrameResult = results.stream()
        .filter(r -> r.getSignalId().equals(sameFrame.getId())).findFirst().orElseThrow();
    assertThat(sameFrameResult.getFrameIdSimilarity()).isEqualTo(1.0);

    var diffFrameResult = results.stream()
        .filter(r -> r.getSignalId().equals(diffFrame.getId())).findFirst().orElseThrow();
    assertThat(diffFrameResult.getFrameIdSimilarity()).isEqualTo(0.0);
  }
}
