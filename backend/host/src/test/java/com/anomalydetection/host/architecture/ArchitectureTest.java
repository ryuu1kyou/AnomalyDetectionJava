package com.anomalydetection.host.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

/**
 * Architecture invariants enforced for the entire AnomalyDetection codebase.
 *
 * <p>これらが一つでも壊れたらビルドが失敗する。M0 では基本ルールのみ:
 *
 * <ol>
 *   <li>{@code web} は {@code infrastructure} を直接参照してはならない
 *   <li>{@code domain} は Spring を参照してはならない (純粋なドメイン層を保つ)
 *   <li>{@code com.anomalydetection.*} 内のパッケージ間に循環依存があってはならない
 *   <li>{@code application} 層は {@code web} に依存してはならない
 * </ol>
 */
@AnalyzeClasses(
    packages = "com.anomalydetection",
    importOptions = {ImportOption.DoNotIncludeTests.class})
class ArchitectureTest {

  @ArchTest
  static final ArchRule webMustNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAPackage("..com.anomalydetection.web..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..com.anomalydetection.infrastructure..");

  @ArchTest
  static final ArchRule domainMustNotDependOnSpring =
      noClasses()
          .that()
          .resideInAPackage("..com.anomalydetection.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.springframework..");

  @ArchTest
  static final ArchRule applicationMustNotDependOnWeb =
      noClasses()
          .that()
          .resideInAPackage("..com.anomalydetection.application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..com.anomalydetection.web..");

  @ArchTest
  static final ArchRule noCyclicDependenciesBetweenModulePackages =
      SlicesRuleDefinition.slices()
          .matching("com.anomalydetection.(*)..")
          .should()
          .beFreeOfCycles();
}
