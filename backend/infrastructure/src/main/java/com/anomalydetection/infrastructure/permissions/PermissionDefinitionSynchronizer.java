package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import com.anomalydetection.contracts.permissions.PermissionDefinitionRegistry;
import com.anomalydetection.contracts.permissions.PermissionGroupDefinition;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class PermissionDefinitionSynchronizer implements ApplicationRunner, PermissionDefinitionRegistry {

  private static final Logger log = LoggerFactory.getLogger(PermissionDefinitionSynchronizer.class);

  private final List<PermissionDefinitionContributor> contributors;
  private PermissionDefinitionContext context = new PermissionDefinitionContext();

  public PermissionDefinitionSynchronizer(List<PermissionDefinitionContributor> contributors) {
    this.contributors = contributors;
  }

  @Override
  public void run(ApplicationArguments args) {
    context = new PermissionDefinitionContext();
    contributors.forEach(c -> c.define(context));
    log.info("Permission definitions loaded: {} permissions from {} contributors",
        context.getAllPermissionNames().size(), contributors.size());
  }

  @Override
  public List<PermissionGroupDefinition> getGroups() {
    return context.getGroups();
  }

  @Override
  public List<String> getAllPermissionNames() {
    return context.getAllPermissionNames();
  }
}
