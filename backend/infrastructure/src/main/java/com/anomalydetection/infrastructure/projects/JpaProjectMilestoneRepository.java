package com.anomalydetection.infrastructure.projects;

import com.anomalydetection.domain.projects.ProjectMilestone;
import com.anomalydetection.domain.projects.ProjectMilestoneRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProjectMilestoneRepository
    extends JpaRepository<ProjectMilestone, UUID>, ProjectMilestoneRepository {

  @Override
  List<ProjectMilestone> findAllByProjectId(UUID projectId);
}
