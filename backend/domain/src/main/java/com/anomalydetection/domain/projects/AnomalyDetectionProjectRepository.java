package com.anomalydetection.domain.projects;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnomalyDetectionProjectRepository
    extends BaseRepository<AnomalyDetectionProject, UUID> {

  List<AnomalyDetectionProject> findAllByStatus(String status);

  Optional<AnomalyDetectionProject> findByProjectCode(String projectCode);

  List<AnomalyDetectionProject> findAllByOemCode(String oemCode);

  /**
   * Searches projects with DB-side filtering and paging.
   *
   * <p>Implemented by the infrastructure repository (Criteria API).
   */
  ProjectSearchResult search(ProjectSearchCriteria criteria);
}
