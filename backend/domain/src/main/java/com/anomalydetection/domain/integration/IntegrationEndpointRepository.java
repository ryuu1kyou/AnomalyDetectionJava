package com.anomalydetection.domain.integration;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface IntegrationEndpointRepository
    extends BaseRepository<IntegrationEndpoint, UUID> {

  List<IntegrationEndpoint> findAllByType(IntegrationType type);

  List<IntegrationEndpoint> findAllByIsActive(boolean isActive);
}
