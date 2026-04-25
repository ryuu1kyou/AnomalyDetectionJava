package com.anomalydetection.contracts.projects;

import java.util.List;

public record UpdateProjectMilestoneDto(
    String name,
    String description,
    String plannedDate,
    String actualDate,
    MilestoneStatus status,
    int progressPercentage,
    List<String> dependencies,
    List<String> deliverables) {}
