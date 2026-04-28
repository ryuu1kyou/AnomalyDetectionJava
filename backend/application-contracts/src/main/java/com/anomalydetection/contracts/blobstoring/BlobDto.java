package com.anomalydetection.contracts.blobstoring;

import java.time.Instant;
import java.util.UUID;

public record BlobDto(
    UUID id,
    String containerName,
    String blobName,
    String mimeType,
    Long sizeBytes,
    Instant createdAt
) {}
