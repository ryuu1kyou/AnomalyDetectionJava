package com.anomalydetection.infrastructure.integration;

import com.anomalydetection.domain.integration.DataImportRequest;
import com.anomalydetection.domain.integration.DataImportRequestRepository;
import com.anomalydetection.domain.integration.ImportStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDataImportRequestRepository
    extends JpaRepository<DataImportRequest, UUID>, DataImportRequestRepository {

  @Override
  List<DataImportRequest> findAllByEndpointId(UUID endpointId);

  @Override
  List<DataImportRequest> findAllByStatus(ImportStatus status);
}
