package com.anomalydetection.domain.integration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntegrationLogRepository {
  IntegrationLog save(IntegrationLog log);
  Optional<IntegrationLog> findById(UUID id);
  List<IntegrationLog> findAllByEndpointId(UUID endpointId);
  long countByEndpointId(UUID endpointId);
}
