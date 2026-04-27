package com.anomalydetection.infrastructure.oemtraceability;

import com.anomalydetection.domain.oemtraceability.OemCustomization;
import com.anomalydetection.domain.oemtraceability.OemCustomizationRepository;
import com.anomalydetection.domain.oemtraceability.OemCustomizationStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOemCustomizationRepository
    extends JpaRepository<OemCustomization, UUID>, OemCustomizationRepository {

  @Override
  List<OemCustomization> findAllByOemCode(String oemCode);

  @Override
  List<OemCustomization> findAllByStatus(OemCustomizationStatus status);

  @Override
  List<OemCustomization> findAllByEntityIdAndEntityType(String entityId, String entityType);
}
