package com.anomalydetection.infrastructure.integration;

import com.anomalydetection.domain.integration.WebhookSubscription;
import com.anomalydetection.domain.integration.WebhookSubscriptionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaWebhookSubscriptionRepository
    extends JpaRepository<WebhookSubscription, UUID>, WebhookSubscriptionRepository {

  @Override
  List<WebhookSubscription> findAllByEndpointId(UUID endpointId);
}
