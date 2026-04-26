package com.anomalydetection.infrastructure.multitenancy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Refreshes Hibernate's {@code tenantFilter} on every Spring Data JPA repository method call.
 *
 * <p>The {@link TenantAwareHibernateJpaDialect} only sets the filter at transaction start —
 * insufficient when the current tenant changes mid-transaction (e.g. inside a single
 * {@code @Transactional} test method). This aspect re-applies the filter using the current
 * tenant before each repository call.
 */
@Aspect
@Component
@Order(0)
public class TenantFilterAspect {

  private static final Logger log = LoggerFactory.getLogger(TenantFilterAspect.class);
  private static final String FILTER_NAME = "tenantFilter";
  private static final String PARAM_TENANT_ID = "tenantId";

  @PersistenceContext private EntityManager entityManager;

  @Around("execution(* org.springframework.data.repository.Repository+.*(..))")
  public Object applyTenantFilter(ProceedingJoinPoint pjp) throws Throwable {
    syncFilter();
    return pjp.proceed();
  }

  private void syncFilter() {
    if (entityManager == null) {
      return;
    }
    try {
      Session session = entityManager.unwrap(Session.class);
      CurrentTenantHolder holder = TenantContextHolderProvider.getHolder();
      if (holder != null && holder.isSet()) {
        session.enableFilter(FILTER_NAME)
            .setParameter(PARAM_TENANT_ID, holder.getTenantId().get().toString());
        log.trace("Tenant filter set to {}", holder.getTenantId().get());
      } else {
        session.disableFilter(FILTER_NAME);
        log.trace("Tenant filter disabled (host mode)");
      }
    } catch (Exception e) {
      log.warn("Failed to sync tenant filter: {}", e.getMessage());
    }
  }
}
