package com.anomalydetection.domain.oemtraceability;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface OemCustomizationRepository extends BaseRepository<OemCustomization, UUID> {

  List<OemCustomization> findAllByOemCode(String oemCode);

  List<OemCustomization> findAllByStatus(OemCustomizationStatus status);

  List<OemCustomization> findAllByEntityIdAndEntityType(String entityId, String entityType);
}
