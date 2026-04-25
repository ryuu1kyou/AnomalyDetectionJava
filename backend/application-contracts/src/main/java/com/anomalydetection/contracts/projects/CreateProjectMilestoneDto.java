package com.anomalydetection.contracts.projects;

import java.util.List;

public record CreateProjectMilestoneDto(
    String projectId,
    String name,
    String description,
    String plannedDate,
    List<String> dependencies,
    List<String> deliverables) {}
