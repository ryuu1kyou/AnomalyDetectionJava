package com.anomalydetection.infrastructure.anomalydetection;

import com.anomalydetection.domain.anomalydetection.AnomalyType;
import com.anomalydetection.domain.anomalydetection.CanAnomalyDetectionLogic;
import com.anomalydetection.domain.anomalydetection.CanAnomalyDetectionLogicRepository;
import com.anomalydetection.domain.anomalydetection.DetectionLogicStatus;
import com.anomalydetection.domain.anomalydetection.SharingLevel;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCanAnomalyDetectionLogicRepository
    extends JpaRepository<CanAnomalyDetectionLogic, UUID>, CanAnomalyDetectionLogicRepository {

  @Override
  List<CanAnomalyDetectionLogic> findAllByStatus(DetectionLogicStatus status);

  @Override
  List<CanAnomalyDetectionLogic> findAllByAnomalyType(AnomalyType anomalyType);

  @Override
  List<CanAnomalyDetectionLogic> findAllBySharingLevel(SharingLevel sharingLevel);

  @Override
  List<CanAnomalyDetectionLogic> findAllByVehiclePhaseId(UUID vehiclePhaseId);
}
