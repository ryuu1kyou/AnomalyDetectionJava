package com.anomalydetection.contracts.projects;

import java.util.List;

public record ProjectMilestoneDto(
    String id,
    String projectId,
    String name,
    String description,
    String plannedDate,
    String actualDate,
    MilestoneStatus status,
    int progressPercentage,
    List<String> dependencies,
    List<String> deliverables) {}
