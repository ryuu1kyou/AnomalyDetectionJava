package com.anomalydetection.contracts.projects;

import java.util.List;

public record ProjectMemberDto(
    String id,
    String projectId,
    String userId,
    String userName,
    String email,
    String role,
    List<String> responsibilities,
    String joinedDate,
    String leftDate,
    boolean isActive,
    boolean canEdit,
    boolean canDelete,
    boolean canManageMembers) {}
