package com.anomalydetection.contracts.auditlogging;

import java.time.Instant;
import java.util.UUID;

public record AuditLogDto(
    UUID id,
    UUID userId,
    String userName,
    UUID tenantId,
    String httpMethod,
    String url,
    String actionName,
    Integer httpStatusCode,
    Long executionDuration,
    Instant occurredAt,
    String exceptions
) {}
