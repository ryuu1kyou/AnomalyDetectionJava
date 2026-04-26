package com.anomalydetection.infrastructure.multitenancy;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Static accessor for {@link CurrentTenantHolder} from non-Spring-managed code
 * (e.g. JPA entity listeners).
 *
 * <p>Spring does not inject request-scoped beans into entity listeners, so this
 * provider bridges the gap by holding a reference to the {@code ApplicationContext}.
 */
@Component
public class TenantContextHolderProvider implements ApplicationContextAware {

  private static ApplicationContext ctx;

  @Override
  public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
    ctx = applicationContext;
  }

  /**
   * Returns the current {@link CurrentTenantHolder} bean, or {@code null} if the
   * application context is not yet available.
   */
  public static CurrentTenantHolder getHolder() {
    if (ctx == null) return null;
    try {
      return ctx.getBean(CurrentTenantHolder.class);
    } catch (BeansException e) {
      return null;
    }
  }
}