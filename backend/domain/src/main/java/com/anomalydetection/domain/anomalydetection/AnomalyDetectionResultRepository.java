package com.anomalydetection.domain.anomalydetection;

import com.anomalydetection.domain.base.BaseRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AnomalyDetectionResultRepository
    extends BaseRepository<AnomalyDetectionResult, UUID> {

  List<AnomalyDetectionResult> findAllByDetectionLogicId(UUID detectionLogicId);

  List<AnomalyDetectionResult> findAllByCanSignalId(UUID canSignalId);

  List<AnomalyDetectionResult> findAllByResolutionStatus(ResolutionStatus status);

  List<AnomalyDetectionResult> findAllByAnomalyLevel(AnomalyLevel level);

  List<AnomalyDetectionResult> findAllByDetectedAtBetween(Instant start, Instant end);
}
