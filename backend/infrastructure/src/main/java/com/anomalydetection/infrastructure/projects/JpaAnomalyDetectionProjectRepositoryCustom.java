package com.anomalydetection.infrastructure.projects;

import com.anomalydetection.domain.projects.ProjectSearchCriteria;
import com.anomalydetection.domain.projects.ProjectSearchResult;

/**
 * Custom repository fragment for {@link JpaAnomalyDetectionProjectRepository}.
 *
 * <p>Provides Criteria-API based search without exposing Spring Data Page types to the domain.
 */
public interface JpaAnomalyDetectionProjectRepositoryCustom {

  /**
   * Repository-fragment search.
   *
   * <p>Method name is intentionally different from the domain repository method to avoid
   * ambiguity when Spring creates proxies.
   */
  ProjectSearchResult searchProjects(ProjectSearchCriteria criteria);
}
