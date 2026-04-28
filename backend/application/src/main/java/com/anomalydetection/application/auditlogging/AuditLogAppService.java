package com.anomalydetection.application.auditlogging;

import com.anomalydetection.contracts.auditlogging.AuditLogDto;
import com.anomalydetection.contracts.auditlogging.AuditLogQueryService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditLogAppService {

  private final AuditLogQueryService queryService;

  public AuditLogAppService(AuditLogQueryService queryService) {
    this.queryService = queryService;
  }

  public List<AuditLogDto> getRecent(int limit) {
    return queryService.getRecent(Math.min(limit, 500));
  }
}
