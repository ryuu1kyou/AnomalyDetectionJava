package com.anomalydetection.infrastructure.projects;

import com.anomalydetection.domain.projects.ProjectMember;
import com.anomalydetection.domain.projects.ProjectMemberRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProjectMemberRepository
    extends JpaRepository<ProjectMember, UUID>, ProjectMemberRepository {

  @Override
  List<ProjectMember> findAllByProjectId(UUID projectId);
}
