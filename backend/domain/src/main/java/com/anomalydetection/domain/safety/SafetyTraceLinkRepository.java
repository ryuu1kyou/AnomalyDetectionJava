package com.anomalydetection.domain.safety;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface SafetyTraceLinkRepository extends BaseRepository<SafetyTraceLink, UUID> {

  List<SafetyTraceLink> findAllBySourceRecordId(UUID sourceRecordId);

  List<SafetyTraceLink> findAllByTargetRecordId(UUID targetRecordId);
}
