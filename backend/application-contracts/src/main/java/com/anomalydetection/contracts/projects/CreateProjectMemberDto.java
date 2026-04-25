package com.anomalydetection.contracts.projects;

import java.util.List;

public record CreateProjectMemberDto(
    String projectId,
    String userId,
    String role,
    List<String> responsibilities,
    boolean canEdit,
    boolean canDelete,
    boolean canManageMembers) {}
