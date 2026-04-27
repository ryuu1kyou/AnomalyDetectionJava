package com.anomalydetection.contracts.safety;

public record SafetyTraceLinkDto(
    String id,
    String sourceRecordId,
    String targetRecordId,
    String linkType,
    String relation) {}
