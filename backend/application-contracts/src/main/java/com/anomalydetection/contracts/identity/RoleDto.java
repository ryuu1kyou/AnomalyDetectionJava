package com.anomalydetection.contracts.identity;

import java.util.UUID;

public record RoleDto(
    UUID id,
    UUID tenantId,
    String name,
    boolean isStatic,
    boolean isDefault) {}
