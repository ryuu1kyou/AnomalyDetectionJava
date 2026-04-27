package com.anomalydetection.contracts.integration;

public record WebhookSubscriptionDto(
    String id,
    String endpointId,
    String targetUrl,
    String eventType,
    boolean isActive,
    int maxRetries,
    int timeoutSeconds,
    String lastTriggeredAt,
    int deliverySuccessCount,
    int deliveryFailureCount) {}
