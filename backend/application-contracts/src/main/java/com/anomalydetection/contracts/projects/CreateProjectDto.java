package com.anomalydetection.contracts.projects;

public record CreateProjectDto(
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
    String oemCode,
    String oemName,
    String notes) {}
