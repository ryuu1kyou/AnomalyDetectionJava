package com.anomalydetection.infrastructure.anomalydetection;

import com.anomalydetection.domain.anomalydetection.AnomalyDetectionResult;
import com.anomalydetection.domain.anomalydetection.AnomalyDetectionResultRepository;
import com.anomalydetection.domain.anomalydetection.AnomalyLevel;
import com.anomalydetection.domain.anomalydetection.ResolutionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAnomalyDetectionResultRepository
    extends JpaRepository<AnomalyDetectionResult, UUID>, AnomalyDetectionResultRepository {

  @Override
  List<AnomalyDetectionResult> findAllByDetectionLogicId(UUID detectionLogicId);

  @Override
  List<AnomalyDetectionResult> findAllByCanSignalId(UUID canSignalId);

  @Override
  List<AnomalyDetectionResult> findAllByResolutionStatus(ResolutionStatus status);

  @Override
  List<AnomalyDetectionResult> findAllByAnomalyLevel(AnomalyLevel level);

  @Override
  List<AnomalyDetectionResult> findAllByDetectedAtBetween(Instant start, Instant end);
}
