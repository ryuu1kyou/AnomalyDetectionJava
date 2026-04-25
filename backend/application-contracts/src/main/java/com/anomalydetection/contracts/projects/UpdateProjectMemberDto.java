package com.anomalydetection.contracts.projects;

import java.util.List;

public record UpdateProjectMemberDto(
    String role,
    List<String> responsibilities,
    boolean canEdit,
    boolean canDelete,
    boolean canManageMembers,
    boolean isActive) {}
