package com.anomalydetection.host.support;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Starts an embedded MariaDB instance once per test class and exposes its JDBC URL via
 * {@link System} properties so that {@code @DynamicPropertySource} or
 * {@code application-test.yml}'s {@code TEST_DB_URL} placeholder can pick it up.
 *
 * <p>Docker 不要の {@link ch.vorburger.mariadb4j MariaDB4j} を採用。{@code @SpringBootTest} と
 * 併用するときは static initializer でこの extension を起動するか、
 * 下記 {@link #register(DynamicPropertyRegistry)} を {@code @DynamicPropertySource} から呼び出す。
 */
public class MariaDB4jExtension implements BeforeAllCallback, AfterAllCallback {

  private static volatile DB sharedDb;
  private static volatile String sharedJdbcUrl;
  private static final String DATABASE_NAME = "anomaly_detection_test";

  @Override
  public synchronized void beforeAll(ExtensionContext context) throws Exception {
    if (sharedDb == null) {
      DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
      configBuilder.setPort(0); // free random port
      sharedDb = DB.newEmbeddedDB(configBuilder.build());
      sharedDb.start();
      sharedDb.createDB(DATABASE_NAME);
      sharedJdbcUrl =
          configBuilder.getURL(DATABASE_NAME)
              + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
      System.setProperty("TEST_DB_URL", sharedJdbcUrl);
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
    // Keep DB alive for the JVM lifetime so subsequent test classes share it.
    // It will be stopped by JVM shutdown hooks.
  }

  /** Convenience helper for {@code @DynamicPropertySource} based wiring. */
  public static void register(DynamicPropertyRegistry registry) {
    if (sharedJdbcUrl == null) {
      throw new IllegalStateException(
          "MariaDB4jExtension has not been initialised. Annotate the test class with"
              + " @ExtendWith(MariaDB4jExtension.class)."
      );
    }
    registry.add("spring.datasource.url", () -> sharedJdbcUrl);
    registry.add("spring.datasource.username", () -> "root");
    registry.add("spring.datasource.password", () -> "");
  }
}
