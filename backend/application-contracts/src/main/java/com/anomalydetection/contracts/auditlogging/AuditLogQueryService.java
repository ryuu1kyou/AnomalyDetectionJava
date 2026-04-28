package com.anomalydetection.contracts.auditlogging;

import java.util.List;

public interface AuditLogQueryService {
  List<AuditLogDto> getRecent(int limit);
}
