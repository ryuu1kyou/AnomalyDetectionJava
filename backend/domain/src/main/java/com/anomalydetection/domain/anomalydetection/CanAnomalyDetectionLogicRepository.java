package com.anomalydetection.domain.anomalydetection;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface CanAnomalyDetectionLogicRepository
    extends BaseRepository<CanAnomalyDetectionLogic, UUID> {

  List<CanAnomalyDetectionLogic> findAllByStatus(DetectionLogicStatus status);

  List<CanAnomalyDetectionLogic> findAllByAnomalyType(AnomalyType anomalyType);

  List<CanAnomalyDetectionLogic> findAllBySharingLevel(SharingLevel sharingLevel);

  List<CanAnomalyDetectionLogic> findAllByVehiclePhaseId(UUID vehiclePhaseId);
}
