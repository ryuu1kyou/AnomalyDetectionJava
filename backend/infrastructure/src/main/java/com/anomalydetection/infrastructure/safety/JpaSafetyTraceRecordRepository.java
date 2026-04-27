package com.anomalydetection.infrastructure.safety;

import com.anomalydetection.domain.safety.SafetyApprovalStatus;
import com.anomalydetection.domain.safety.SafetyTraceRecord;
import com.anomalydetection.domain.safety.SafetyTraceRecordRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSafetyTraceRecordRepository
    extends JpaRepository<SafetyTraceRecord, UUID>, SafetyTraceRecordRepository {

  @Override
  List<SafetyTraceRecord> findAllByAsilLevel(String asilLevel);

  @Override
  List<SafetyTraceRecord> findAllByApprovalStatus(SafetyApprovalStatus status);

  @Override
  List<SafetyTraceRecord> findAllByProjectId(UUID projectId);

  @Override
  List<SafetyTraceRecord> findAllByDetectionLogicId(UUID detectionLogicId);
}
