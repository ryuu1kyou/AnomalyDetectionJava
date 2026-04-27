package com.anomalydetection.infrastructure.integration;

import com.anomalydetection.domain.integration.IntegrationLog;
import com.anomalydetection.domain.integration.IntegrationLogRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaIntegrationLogRepository
    extends JpaRepository<IntegrationLog, UUID>, IntegrationLogRepository {

  @Override
  List<IntegrationLog> findAllByEndpointId(UUID endpointId);

  @Override
  long countByEndpointId(UUID endpointId);
}
