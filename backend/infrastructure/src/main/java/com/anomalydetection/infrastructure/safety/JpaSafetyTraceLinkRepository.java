package com.anomalydetection.infrastructure.safety;

import com.anomalydetection.domain.safety.SafetyTraceLink;
import com.anomalydetection.domain.safety.SafetyTraceLinkRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSafetyTraceLinkRepository
    extends JpaRepository<SafetyTraceLink, UUID>, SafetyTraceLinkRepository {

  @Override
  List<SafetyTraceLink> findAllBySourceRecordId(UUID sourceRecordId);

  @Override
  List<SafetyTraceLink> findAllByTargetRecordId(UUID targetRecordId);
}
