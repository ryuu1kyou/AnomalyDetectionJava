package com.anomalydetection.contracts.safety;

import com.anomalydetection.shared.safety.ChangeType;

public record AddChangeRequestDto(
    String changeId,
    ChangeType changeType,
    String description,
    String rationale) {}
