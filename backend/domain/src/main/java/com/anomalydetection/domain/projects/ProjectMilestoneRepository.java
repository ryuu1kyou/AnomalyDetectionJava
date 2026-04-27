package com.anomalydetection.domain.projects;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface ProjectMilestoneRepository extends BaseRepository<ProjectMilestone, UUID> {

  List<ProjectMilestone> findAllByProjectId(UUID projectId);
}
