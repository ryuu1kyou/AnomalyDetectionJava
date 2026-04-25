package com.anomalydetection.contracts.projects;

public record ProjectDto(
    String id,
    String projectCode,
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
    int totalDetectionLogics,
    int totalCanSignals,
    int totalAnomalies,
    int resolvedAnomalies) {}
