package com.anomalydetection.contracts.safety;

public record CreateSafetyTraceLinkDto(
    String sourceRecordId,
    String targetRecordId,
    String linkType,
    String relation) {}
