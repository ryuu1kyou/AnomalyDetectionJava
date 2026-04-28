package com.anomalydetection.web.auditlogging;

import com.anomalydetection.application.auditlogging.AuditLogAppService;
import com.anomalydetection.contracts.auditlogging.AuditLogDto;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/audit-logs")
public class AuditLogController {

  private final AuditLogAppService service;

  public AuditLogController(AuditLogAppService service) {
    this.service = service;
  }

  @GetMapping
  public List<AuditLogDto> getRecent(@RequestParam(defaultValue = "100") int limit) {
    return service.getRecent(limit);
  }
}
