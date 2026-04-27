package com.anomalydetection.infrastructure.projects;

import com.anomalydetection.domain.projects.AnomalyDetectionProject;
import com.anomalydetection.domain.projects.AnomalyDetectionProjectRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAnomalyDetectionProjectRepository
    extends JpaRepository<AnomalyDetectionProject, UUID>, AnomalyDetectionProjectRepository {

  @Override
  List<AnomalyDetectionProject> findAllByStatus(String status);

  @Override
  Optional<AnomalyDetectionProject> findByProjectCode(String projectCode);

  @Override
  List<AnomalyDetectionProject> findAllByOemCode(String oemCode);
}
