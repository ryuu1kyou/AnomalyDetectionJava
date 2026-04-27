package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class PermissionDefinitionSynchronizer implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(PermissionDefinitionSynchronizer.class);

  private final List<PermissionDefinitionContributor> contributors;

  public PermissionDefinitionSynchronizer(List<PermissionDefinitionContributor> contributors) {
    this.contributors = contributors;
  }

  @Override
  public void run(ApplicationArguments args) {
    var context = new PermissionDefinitionContext();
    contributors.forEach(c -> c.define(context));
    var allNames = context.getAllPermissionNames();
    log.info("Permission definitions loaded: {} permissions from {} contributors",
        allNames.size(), contributors.size());
  }
}