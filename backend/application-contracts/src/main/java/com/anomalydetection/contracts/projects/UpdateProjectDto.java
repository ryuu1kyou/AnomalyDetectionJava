package com.anomalydetection.contracts.projects;

public record UpdateProjectDto(
    String projectName,
    String description,
    String vehicleModel,
    String modelYear,
    String platform,
    String primarySystem,
    String targetMarket,
    ProjectStatus status,
    ProjectPriority priority,
    String startDate,
    String plannedEndDate,
    String actualEndDate,
    int progressPercentage,
    String oemCode,
    String oemName,
    String notes) {}
