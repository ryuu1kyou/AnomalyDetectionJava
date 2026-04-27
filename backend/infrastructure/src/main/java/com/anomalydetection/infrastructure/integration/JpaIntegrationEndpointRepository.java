package com.anomalydetection.infrastructure.integration;

import com.anomalydetection.domain.integration.IntegrationEndpoint;
import com.anomalydetection.domain.integration.IntegrationEndpointRepository;
import com.anomalydetection.domain.integration.IntegrationType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaIntegrationEndpointRepository
    extends JpaRepository<IntegrationEndpoint, UUID>, IntegrationEndpointRepository {

  @Override
  List<IntegrationEndpoint> findAllByType(IntegrationType type);

  @Override
  List<IntegrationEndpoint> findAllByIsActive(boolean isActive);
}
