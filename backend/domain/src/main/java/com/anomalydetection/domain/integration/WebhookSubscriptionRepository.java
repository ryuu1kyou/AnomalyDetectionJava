package com.anomalydetection.domain.integration;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface WebhookSubscriptionRepository
    extends BaseRepository<WebhookSubscription, UUID> {

  List<WebhookSubscription> findAllByEndpointId(UUID endpointId);
}
