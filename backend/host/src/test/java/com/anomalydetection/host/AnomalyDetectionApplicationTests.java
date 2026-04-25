package com.anomalydetection.host;

import static org.assertj.core.api.Assertions.assertThat;

import com.anomalydetection.AnomalyDetectionApplication;
import com.anomalydetection.host.support.MariaDB4jExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Smoke test that verifies the Spring application context boots end-to-end with
 * the MariaDB4j-backed datasource and Liquibase wiring.
 */
@SpringBootTest(classes = AnomalyDetectionApplication.class)
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class AnomalyDetectionApplicationTests {

  @Autowired private ApplicationContext context;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void contextLoads() {
    assertThat(context).isNotNull();
    assertThat(context.getApplicationName()).isNotNull();
  }
}
