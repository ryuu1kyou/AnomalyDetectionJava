package com.anomalydetection.domain.integration;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface DataImportRequestRepository
    extends BaseRepository<DataImportRequest, UUID> {

  List<DataImportRequest> findAllByEndpointId(UUID endpointId);

  List<DataImportRequest> findAllByStatus(ImportStatus status);
}
