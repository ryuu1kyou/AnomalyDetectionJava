package com.anomalydetection.domain.projects;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface ProjectMemberRepository extends BaseRepository<ProjectMember, UUID> {

  List<ProjectMember> findAllByProjectId(UUID projectId);
}
