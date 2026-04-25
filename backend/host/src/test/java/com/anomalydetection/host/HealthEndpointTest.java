package com.anomalydetection.host;

import static org.assertj.core.api.Assertions.assertThat;

import com.anomalydetection.AnomalyDetectionApplication;
import com.anomalydetection.host.support.MariaDB4jExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Verifies that the Actuator health endpoint reports {@code UP} when the application
 * boots end-to-end with MariaDB4j + Liquibase.
 */
@SpringBootTest(
    classes = AnomalyDetectionApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class HealthEndpointTest {

  @LocalServerPort private int port;
  @Autowired private TestRestTemplate restTemplate;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void healthEndpointReportsUp() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("http://localhost:" + port + "/actuator/health", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("\"status\":\"UP\"");
  }
}
