package com.anomalydetection.contracts.integration;

public record CreateWebhookSubscriptionDto(
    String targetUrl,
    String eventType,
    boolean isActive) {}
