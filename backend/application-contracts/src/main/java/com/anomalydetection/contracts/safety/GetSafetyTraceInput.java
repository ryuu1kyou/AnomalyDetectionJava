package com.anomalydetection.contracts.safety;

public record GetSafetyTraceInput(
    String filter,
    String asilLevel,
    String approvalStatus,
    String projectId,
    String detectionLogicId,
    Integer skipCount,
    Integer maxResultCount) {}
