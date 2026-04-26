package com.anomalydetection.infrastructure.multitenancy;

import jakarta.persistence.EntityManager;
import java.sql.SQLException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

public class TenantAwareHibernateJpaDialect extends HibernateJpaDialect {

  private static final Logger log = LoggerFactory.getLogger(TenantAwareHibernateJpaDialect.class);
  private static final String FILTER_NAME = "tenantFilter";
  private static final String PARAM_TENANT_ID = "tenantId";

  @Override
  public Object beginTransaction(EntityManager em, TransactionDefinition definition)
      throws SQLException, TransactionException {
    Object data = super.beginTransaction(em, definition);
    enableTenantFilter(em);
    return data;
  }

  private void enableTenantFilter(EntityManager em) {
    CurrentTenantHolder holder = TenantContextHolderProvider.getHolder();
    if (holder == null || !holder.isSet()) {
      return;
    }
    try {
      Session session = em.unwrap(Session.class);
      session.enableFilter(FILTER_NAME)
          .setParameter(PARAM_TENANT_ID, holder.getTenantId().get().toString());
      log.trace("Tenant filter enabled for tenant {}", holder.getTenantId().get());
    } catch (Exception e) {
      log.warn("Failed to enable tenant filter: {}", e.getMessage());
    }
  }
}
