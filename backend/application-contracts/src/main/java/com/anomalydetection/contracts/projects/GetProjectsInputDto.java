package com.anomalydetection.contracts.projects;

/** Query DTO for project list (ABP GetList style). */
public record GetProjectsInputDto(
    String filter,
    Integer status,
    Integer priority,
    String vehicleModel,
    String primarySystem,
    Integer skipCount,
    Integer maxResultCount,
    String sorting) {}
