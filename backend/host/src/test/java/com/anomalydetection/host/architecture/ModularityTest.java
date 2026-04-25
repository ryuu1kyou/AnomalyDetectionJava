package com.anomalydetection.host.architecture;

import com.anomalydetection.AnomalyDetectionApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Spring Modulith 構造検証テスト。
 *
 * <p>{@link ApplicationModules#verify()} はモジュール境界違反 (例えば下位レイヤーが上位レイヤーの
 * 内部実装を直接参照しているケース) を検出してビルドを失敗させる。
 *
 * <p>{@link Documenter} は PlantUML 図と AsciiDoc を {@code target/spring-modulith-docs/}
 * に出力する。CI で差分管理することで構造変化を可視化できる。
 */
class ModularityTest {

  private final ApplicationModules modules = ApplicationModules.of(AnomalyDetectionApplication.class);

  @Test
  void verifyModularStructure() {
    modules.verify();
  }

  @Test
  void writeDocumentationSnapshot() {
    new Documenter(modules).writeDocumentation();
  }
}
