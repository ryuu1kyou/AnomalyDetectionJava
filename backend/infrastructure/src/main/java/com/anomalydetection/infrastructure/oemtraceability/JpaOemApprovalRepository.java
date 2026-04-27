package com.anomalydetection.infrastructure.oemtraceability;

import com.anomalydetection.domain.oemtraceability.OemApproval;
import com.anomalydetection.domain.oemtraceability.OemApprovalRepository;
import com.anomalydetection.domain.oemtraceability.OemApprovalStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOemApprovalRepository
    extends JpaRepository<OemApproval, UUID>, OemApprovalRepository {

  @Override
  List<OemApproval> findAllByOemCode(String oemCode);

  @Override
  List<OemApproval> findAllByStatus(OemApprovalStatus status);

  @Override
  List<OemApproval> findAllByEntityIdAndEntityType(String entityId, String entityType);
}
