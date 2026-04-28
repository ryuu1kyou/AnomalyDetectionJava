package com.anomalydetection.infrastructure.audit;

import com.anomalydetection.contracts.auditlogging.AuditLogDto;
import com.anomalydetection.contracts.auditlogging.AuditLogQueryService;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditLogQueryServiceImpl implements AuditLogQueryService {

  private final JpaAuditLogRepository repository;

  public AuditLogQueryServiceImpl(JpaAuditLogRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<AuditLogDto> getRecent(int limit) {
    return repository.findAllByOrderByOccurredAtDesc(PageRequest.of(0, limit))
        .stream()
        .map(e -> new AuditLogDto(
            e.getId(), e.getUserId(), e.getUserName(), e.getTenantId(),
            e.getHttpMethod(), e.getUrl(), e.getActionName(),
            e.getHttpStatusCode(), e.getExecutionDuration(),
            e.getOccurredAt(), e.getExceptions()))
        .toList();
  }
}
