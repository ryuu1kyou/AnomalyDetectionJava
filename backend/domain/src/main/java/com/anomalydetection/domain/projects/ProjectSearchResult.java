package com.anomalydetection.domain.projects;

import java.util.List;

/** Result for {@link AnomalyDetectionProjectRepository#search(ProjectSearchCriteria)}. */
public record ProjectSearchResult(
    List<AnomalyDetectionProject> items,
    long totalCount) {
}
