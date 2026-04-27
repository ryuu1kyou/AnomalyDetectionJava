package com.anomalydetection.infrastructure.audit;

import com.anomalydetection.infrastructure.multitenancy.CurrentTenantHolder;
import com.anomalydetection.shared.CurrentUserIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditLogAspect {

  private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

  private final JpaAuditLogRepository repository;
  private final CurrentTenantHolder currentTenantHolder;

  public AuditLogAspect(JpaAuditLogRepository repository, CurrentTenantHolder currentTenantHolder) {
    this.repository = repository;
    this.currentTenantHolder = currentTenantHolder;
  }

  @Around("within(@org.springframework.web.bind.annotation.RestController *)")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Object audit(ProceedingJoinPoint pjp) throws Throwable {
    var start = Instant.now();
    var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest req = attrs != null ? attrs.getRequest() : null;

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UUID userId = CurrentUserIdHolder.getUserId().orElse(null);
    String userName = auth != null ? auth.getName() : null;
    UUID tenantId = currentTenantHolder.getTenantId().orElse(null);

    String method = req != null ? req.getMethod() : null;
    String url = req != null ? req.getRequestURI() : null;
    String actionName = pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();

    AuditLogEntity entry = new AuditLogEntity(userId, userName, tenantId, method, url, actionName, start);

    Throwable thrown = null;
    try {
      Object result = pjp.proceed();
      return result;
    } catch (Throwable t) {
      thrown = t;
      entry.setExceptions(t.getClass().getName() + ": " + t.getMessage());
      throw t;
    } finally {
      long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
      entry.setExecutionDuration(duration);
      try {
        repository.save(entry);
      } catch (Exception ex) {
        log.warn("Failed to save audit log entry", ex);
      }
    }
  }
}