package com.anomalydetection.host.support;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
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
      // Windows + surefire fork で MariaDB4j が JVM shutdown hook 経由で temp 配下を削除しようとすると
      // ファイルロック等で shutdown が長引き、"Surefire is going to kill self fork JVM" になりやすい。
      // そのため、テスト用 DB のディレクトリを OS の temp 配下ではなく module の target 配下に作成する。
      // (target 配下なら "temporary directory" 判定に引っかからず、shutdown hook 側の削除処理も走らない)
      Path root = Path.of(System.getProperty("user.dir"), "target", "mariadb4j");
      Files.createDirectories(root);

      // data/tmp は衝突回避のためユニークに。
      String runId = UUID.randomUUID().toString();

      DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
      configBuilder.setPort(0); // free random port
      configBuilder.setBaseDir(root.resolve("base").toString());
      configBuilder.setDataDir(root.resolve("data").resolve(runId).toString());
      configBuilder.setTmpDir(root.resolve("tmp").resolve(runId).toString());
      // NOTE:
      // MariaDB4j の「終了時に temp ディレクトリを削除する」処理は Windows だと遅くなりやすく、
      // surefire の fork JVM が System.exit(0) 後 30 秒で kill される原因になりうる。
      // テスト安定性を優先して削除を無効化し、OS の temp cleanup に任せる。
      configBuilder.setDeletingTemporaryBaseAndDataDirsOnShutdown(false);
      sharedDb = DB.newEmbeddedDB(configBuilder.build());
      sharedDb.start();
      sharedDb.createDB(DATABASE_NAME);

      // MariaDB4j のデフォルト URL は jdbc:mariadb://...
      // Spring Boot の auto-detect driver が org.mariadb.jdbc.Driver を要求するのを避けるため、
      // テストでは MySQL driver (mysql-connector-j) で接続できる jdbc:mysql://... に寄せる。
      String url = configBuilder.getURL(DATABASE_NAME);
      if (url.startsWith("jdbc:mariadb://")) {
        url = url.replaceFirst("jdbc:mariadb://", "jdbc:mysql://");
      }

      sharedJdbcUrl = url + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
      System.setProperty("TEST_DB_URL", sharedJdbcUrl);
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
    // NOTE:
    // 以前は JVM shutdown hook に任せて DB を停止していたが、
    // MariaDB4j/commons-exec が生成する非 daemon thread が残り、
    // Surefire の fork JVM が 30 秒で kill されることがあった。
    //
    // そのため、テストクラス単位で確実に stop() する。
    // (速度は落ちるが、CI/ローカルでの安定性を優先)
    synchronized (MariaDB4jExtension.class) {
      if (sharedDb != null) {
        try {
          sharedDb.stop();
        } catch (Exception ignored) {
          // ignore
        } finally {
          sharedDb = null;
          sharedJdbcUrl = null;
          System.clearProperty("TEST_DB_URL");
        }
      }
    }
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
