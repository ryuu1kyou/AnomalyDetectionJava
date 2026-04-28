package com.anomalydetection.host.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

@AnalyzeClasses(
    packages = "com.anomalydetection",
    importOptions = {ImportOption.DoNotIncludeTests.class})
class ArchitectureTest {

  // ── 既存ルール ──────────────────────────────────────────────────────────────

  @ArchTest
  static final ArchRule webMustNotDependOnInfrastructure =
      noClasses()
          .that().resideInAPackage("..com.anomalydetection.web..")
          .should().dependOnClassesThat()
          .resideInAPackage("..com.anomalydetection.infrastructure..");

  @ArchTest
  static final ArchRule domainMustNotDependOnSpring =
      noClasses()
          .that().resideInAPackage("..com.anomalydetection.domain..")
          .should(
              dependOnClassesThat(
                  resideInAPackage("org.springframework..")
                      .and(not(resideInAPackage("org.springframework.modulith..")))
                      .as("Spring classes outside of Spring Modulith")));

  @ArchTest
  static final ArchRule applicationMustNotDependOnWeb =
      noClasses()
          .that().resideInAPackage("..com.anomalydetection.application..")
          .should().dependOnClassesThat()
          .resideInAPackage("..com.anomalydetection.web..");

  @ArchTest
  static final ArchRule noCyclicDependenciesBetweenModulePackages =
      SlicesRuleDefinition.slices()
          .matching("com.anomalydetection.(*)..")
          .should().beFreeOfCycles();

  // ── 追加ルール ──────────────────────────────────────────────────────────────

  /** DTO (〜Dto) は application-contracts パッケージ内のみで定義可 */
  @ArchTest
  static final ArchRule dtosMustResideInContracts =
      classes()
          .that().haveSimpleNameEndingWith("Dto")
          .and().areNotInterfaces()
          .should().resideInAPackage("..com.anomalydetection.contracts..");

  /** domain-shared は domain / application / infrastructure を参照してはならない */
  @ArchTest
  static final ArchRule domainSharedMustNotDependOnHigherLayers =
      noClasses()
          .that().resideInAPackage("..com.anomalydetection.shared..")
          .should().dependOnClassesThat()
          .resideInAnyPackage(
              "..com.anomalydetection.domain..",
              "..com.anomalydetection.application..",
              "..com.anomalydetection.infrastructure..",
              "..com.anomalydetection.web..");

  /** infrastructure は web を直接参照してはならない */
  @ArchTest
  static final ArchRule infrastructureMustNotDependOnWeb =
      noClasses()
          .that().resideInAPackage("..com.anomalydetection.infrastructure..")
          .should().dependOnClassesThat()
          .resideInAPackage("..com.anomalydetection.web..");

  /** AppService クラスは application パッケージ内にのみ存在すること */
  @ArchTest
  static final ArchRule appServicesMustResideInApplication =
      classes()
          .that().haveSimpleNameEndingWith("AppService")
          .and().areNotInterfaces()
          .should().resideInAPackage("..com.anomalydetection.application..");
}
