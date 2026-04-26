package com.anomalydetection.contracts.identity;

import java.util.UUID;

public record UserDto(
    UUID id,
    UUID tenantId,
    String userName,
    String email,
    boolean isActive,
    boolean emailConfirmed) {}
