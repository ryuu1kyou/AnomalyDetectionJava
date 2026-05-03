package com.anomalydetection.domain.oemtraceability;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface OemApprovalRepository extends BaseRepository<OemApproval, UUID> {

  List<OemApproval> findAllByOemCode(String oemCode);

  List<OemApproval> findAllByStatus(OemApprovalStatus status);

  List<OemApproval> findAllByEntityIdAndEntityType(String entityId, String entityType);

  List<OemApproval> findAllByFeatureId(String featureId);
}
