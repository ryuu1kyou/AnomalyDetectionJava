package com.anomalydetection.web.similarpatternsearch;

import com.anomalydetection.application.similarpatternsearch.SimilarPatternSearchAppService;
import com.anomalydetection.contracts.similarpatternsearch.SimilarSignalResultDto;
import com.anomalydetection.contracts.similarpatternsearch.SimilarSignalSearchRequestDto;
import com.anomalydetection.contracts.similarpatternsearch.TestDataComparisonDto;
import com.anomalydetection.contracts.similarpatternsearch.TestDataComparisonRequestDto;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/similar-pattern-search")
public class SimilarPatternSearchController {

  private final SimilarPatternSearchAppService appService;

  public SimilarPatternSearchController(SimilarPatternSearchAppService appService) {
    this.appService = appService;
  }

  @PostMapping("/signals")
  public List<SimilarSignalResultDto> searchSimilarSignals(
      @RequestBody SimilarSignalSearchRequestDto request) {
    return appService.searchSimilarSignals(request);
  }

  @PostMapping("/test-data/compare")
  public TestDataComparisonDto compareTestData(
      @RequestBody TestDataComparisonRequestDto request) {
    return appService.compareTestData(request);
  }

  @GetMapping("/recommendations/{signalId}")
  public List<SimilarSignalResultDto> getRecommendations(
      @PathVariable UUID signalId,
      @RequestParam(defaultValue = "10") int maxRecommendations) {
    return appService.getRecommendations(signalId, maxRecommendations);
  }
}
