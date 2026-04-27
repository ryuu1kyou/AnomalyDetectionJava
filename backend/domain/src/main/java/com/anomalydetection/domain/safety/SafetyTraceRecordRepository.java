package com.anomalydetection.domain.safety;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface SafetyTraceRecordRepository extends BaseRepository<SafetyTraceRecord, UUID> {

  List<SafetyTraceRecord> findAllByAsilLevel(String asilLevel);

  List<SafetyTraceRecord> findAllByApprovalStatus(SafetyApprovalStatus status);

  List<SafetyTraceRecord> findAllByProjectId(UUID projectId);

  List<SafetyTraceRecord> findAllByDetectionLogicId(UUID detectionLogicId);
}
