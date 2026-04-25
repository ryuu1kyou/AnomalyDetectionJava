package com.anomalydetection.host.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.anomalydetection.AnomalyDetectionApplication;
import com.anomalydetection.host.support.MariaDB4jExtension;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Verifies that Liquibase ran during application startup and created its
 * {@code DATABASECHANGELOG} bookkeeping tables.
 */
@SpringBootTest(classes = AnomalyDetectionApplication.class)
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class LiquibaseStartupTest {

  @Autowired private DataSource dataSource;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void liquibaseCreatesBookkeepingTables() throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      var rs =
          connection
              .getMetaData()
              .getTables(connection.getCatalog(), null, "DATABASECHANGELOG%", new String[] {"TABLE"});

      int count = 0;
      while (rs.next()) {
        count++;
      }
      assertThat(count)
          .as("Liquibase should create DATABASECHANGELOG and DATABASECHANGELOGLOCK")
          .isEqualTo(2);
    }
  }
}
