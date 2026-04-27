package com.anomalydetection.contracts.cansignals;

import java.util.UUID;

public record CreateUpdateCanSignalDto(
    int frameId,
    String name,
    String description,
    int startBit,
    int length,
    String byteOrder,
    boolean isSigned,
    UUID specificationId
) {}