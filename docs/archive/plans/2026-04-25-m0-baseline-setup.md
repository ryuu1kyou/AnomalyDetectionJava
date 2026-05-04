# M0 基盤セットアップ Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** AnomalyDetectionJava の Maven 9 サブモジュール骨格を作成し、空の Spring Boot アプリが MySQL 8 に接続して Liquibase の空 changelog を実行し、Actuator `/actuator/health` を返し、ArchUnit + Spring Modulith のモジュール境界テストが緑になる状態を作る。

**Architecture:** Maven multi-module + Spring Boot 3.3 + Spring Modulith。9 モジュール (`domain-shared`, `domain`, `application-contracts`, `application`, `infrastructure`, `web`, `auth-server`, `host`, `db-migrator`) を spec の依存方向どおりに構成。テストは MariaDB4j (組み込み MariaDB / Docker 不要) で実 DB 相当のシナリオを検証。

**Tech Stack:**
- Java 21 (Eclipse Temurin)
- Maven 3.9.x (Wrapper 同梱)
- Spring Boot 3.3.5 + Spring Modulith 1.3.1
- Spring Data JPA + Hibernate 6 (Spring Boot BOM)
- Liquibase (Spring Boot BOM)
- MySQL 8 (本番/開発)、MariaDB4j 3.1.0 (テスト)
- ArchUnit 1.3.0
- JUnit 5 + AssertJ (Spring Boot BOM)
- MapStruct 1.6.2 / Lombok (Spring Boot BOM) — 親 POM に登録、M0 では未使用

**Spec参照:** [docs/superpowers/specs/2026-04-25-anomaly-detection-java-port-design.md](../specs/2026-04-25-anomaly-detection-java-port-design.md)

**M0 完了条件 (Done When):**
1. `c:/work/tool/net/AnomalyDetectionJava/` が独立した Git リポジトリになっている
2. `cd backend && ./mvnw -B verify` が緑で完走する
3. `./mvnw -pl host -am spring-boot:run -Dspring-boot.run.profiles=local` でアプリが起動し、`http://localhost:44397/actuator/health` が `{"status":"UP"}` を返す
4. ArchUnit 4 ルール + Spring Modulith verify テストが PASS する
5. Liquibase 空 changelog がアプリ起動とテストの両方で実行される

---

## ファイル構成 (M0 終了時点)

```
AnomalyDetectionJava/
├── .gitignore                                       # NEW
├── README.md                                        # NEW
├── CLAUDE.md                                        # 既存
├── docs/superpowers/
│   ├── specs/2026-04-25-anomaly-detection-java-port-design.md   # 既存
│   └── plans/2026-04-25-m0-baseline-setup.md                    # 既存 (本計画)
└── backend/
    ├── .mvn/wrapper/maven-wrapper.properties        # NEW (Maven Wrapper)
    ├── mvnw                                         # NEW (Unix wrapper)
    ├── mvnw.cmd                                     # NEW (Windows wrapper)
    ├── pom.xml                                      # NEW (parent, packaging=pom)
    │
    ├── domain-shared/
    │   ├── pom.xml                                  # NEW
    │   └── src/main/java/com/anomalydetection/shared/package-info.java          # NEW
    │
    ├── domain/
    │   ├── pom.xml                                  # NEW
    │   └── src/main/java/com/anomalydetection/domain/package-info.java          # NEW
    │
    ├── application-contracts/
    │   ├── pom.xml                                  # NEW
    │   └── src/main/java/com/anomalydetection/contracts/package-info.java       # NEW
    │
    ├── application/
    │   ├── pom.xml                                  # NEW
    │   └── src/main/java/com/anomalydetection/application/package-info.java     # NEW
    │
    ├── infrastructure/
    │   ├── pom.xml                                  # NEW
    │   ├── src/main/java/com/anomalydetection/infrastructure/InfrastructureConfiguration.java   # NEW
    │   └── src/main/resources/db/changelog/db.changelog-master.yaml             # NEW (空 changelog)
    │
    ├── web/
    │   ├── pom.xml                                  # NEW
    │   └── src/main/java/com/anomalydetection/web/package-info.java             # NEW
    │
    ├── auth-server/
    │   ├── pom.xml                                  # NEW
    │   └── src/main/java/com/anomalydetection/authserver/package-info.java      # NEW
    │
    ├── host/
    │   ├── pom.xml                                  # NEW
    │   ├── src/main/java/com/anomalydetection/AnomalyDetectionApplication.java  # NEW
    │   ├── src/main/resources/application.yml                                   # NEW (デフォルト)
    │   ├── src/main/resources/application-local.yml                             # NEW (ローカル MySQL)
    │   ├── src/main/resources/application-test.yml                              # NEW (MariaDB4j)
    │   ├── src/test/java/com/anomalydetection/host/AnomalyDetectionApplicationTests.java   # NEW
    │   ├── src/test/java/com/anomalydetection/host/HealthEndpointTest.java                 # NEW
    │   ├── src/test/java/com/anomalydetection/host/architecture/ArchitectureTest.java      # NEW
    │   ├── src/test/java/com/anomalydetection/host/architecture/ModularityTest.java        # NEW
    │   ├── src/test/java/com/anomalydetection/host/architecture/LiquibaseStartupTest.java  # NEW
    │   └── src/test/java/com/anomalydetection/host/support/MariaDB4jExtension.java         # NEW
    │
    └── db-migrator/
        ├── pom.xml                                  # NEW
        └── src/main/java/com/anomalydetection/dbmigrator/package-info.java      # NEW
```

すべての空の機能パッケージは `package-info.java` で表現 (Java は空ディレクトリを保持しないため)。

---

## モジュール依存関係 (M0 で確立)

```text
domain-shared
   ↑
domain
   ↑
application-contracts
   ↑
application ──→ infrastructure ──→ host
   ↑                                ↑
   └──── web ────────────────────────┘
                                  ↑
                       auth-server, db-migrator も host が組み立て
```

Maven の `<dependency>` で表現。ArchUnit は **同一 Java パッケージ階層** での違反 (例: `web` パッケージから `infrastructure` パッケージ) を検証する。

---

## Task 1: バックエンドディレクトリと Git リポジトリ初期化

**Files:**
- Create: `c:/work/tool/net/AnomalyDetectionJava/.gitignore`
- Create: `c:/work/tool/net/AnomalyDetectionJava/README.md`
- Create: `c:/work/tool/net/AnomalyDetectionJava/backend/` (空ディレクトリ)
- Init: `c:/work/tool/net/AnomalyDetectionJava/` を新しい Git リポジトリとして初期化

- [ ] **Step 1: backend ディレクトリを作成**

```bash
mkdir -p c:/work/tool/net/AnomalyDetectionJava/backend
```

- [ ] **Step 2: ルート .gitignore を作成**

ファイル: `c:/work/tool/net/AnomalyDetectionJava/.gitignore`

```gitignore
# Java / Maven
target/
*.class
*.jar
*.war
*.ear
*.log

# Maven Wrapper
!.mvn/wrapper/maven-wrapper.jar
.mvn/wrapper/MavenWrapperDownloader.java

# IDE
.idea/
*.iml
*.iws
*.ipr
.vscode/
.classpath
.project
.settings/

# OS
.DS_Store
Thumbs.db

# Frontend (将来用)
node_modules/
dist/
.cache/
.parcel-cache/
*.tsbuildinfo
.env
.env.local

# Logs
logs/

# MariaDB4j テンポラリ
target/mariadb4j/

# Liquibase ローカル lock
db.lck
```

- [ ] **Step 3: ルート README.md を作成**

ファイル: `c:/work/tool/net/AnomalyDetectionJava/README.md`

````markdown
# AnomalyDetection (Java 移植版)

`.NET 10 + ABP vNext 9.3.5` で実装された CAN 異常検出管理システムを
**Java 21 + Spring Boot 3.3 + React** に完全機能パリティ移植したリポジトリ。

詳細は以下を参照:

- プロジェクトメモ (Claude Code 用): [CLAUDE.md](CLAUDE.md)
- 設計書: [docs/superpowers/specs/2026-04-25-anomaly-detection-java-port-design.md](docs/superpowers/specs/2026-04-25-anomaly-detection-java-port-design.md)
- 実装計画: [docs/superpowers/plans/](docs/superpowers/plans/)

## 開発前提

- Java 21 (Eclipse Temurin 推奨)
- Maven 3.9.x (Wrapper `./mvnw` 同梱、別途インストール不要)
- Node.js 20.x (フロントエンド開発時)
- MySQL 8.x (PC に直接インストール、Docker 不使用)

## ローカル起動

```bash
cd backend

# 1. ローカル MySQL に DB を作成 (初回のみ)
mysql -u root -p -e "CREATE DATABASE anomaly_detection CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

# 2. バックエンド起動
./mvnw -pl host -am spring-boot:run -Dspring-boot.run.profiles=local
```

`http://localhost:44397/actuator/health` でヘルスチェックを確認。

## テスト

```bash
cd backend
./mvnw -B verify
```
````

- [ ] **Step 4: Git リポジトリ初期化**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git init && git add .gitignore README.md CLAUDE.md docs/
```

期待される出力: `Initialized empty Git repository in c:/work/tool/net/AnomalyDetectionJava/.git/`

- [ ] **Step 5: 初期コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git commit -m "chore: initialize repository with spec and plan"
```

期待される出力: 4 ファイル (CLAUDE.md, README.md, .gitignore, spec, plan) を含むコミット

---

## Task 2: Maven Wrapper をセットアップ

**Files:**
- Create: `backend/.mvn/wrapper/maven-wrapper.properties`
- Create: `backend/mvnw`
- Create: `backend/mvnw.cmd`

- [ ] **Step 1: Maven Wrapper ファイルを生成**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && mvn wrapper:wrapper -Dmaven=3.9.9
```

注意: ホスト OS に Maven (`mvn`) がインストールされている必要がある。インストールされていない場合は手動で以下のファイルを作成 (Spring Initializr ダウンロードからコピー可)。

期待される出力: `BUILD SUCCESS` と `backend/mvnw`, `backend/mvnw.cmd`, `backend/.mvn/wrapper/maven-wrapper.properties` の生成

- [ ] **Step 2: Wrapper 動作確認**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -v
```

期待される出力: `Apache Maven 3.9.9` と `Java version: 21.x.x` を含むバージョン情報

- [ ] **Step 3: コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git add backend/.mvn backend/mvnw backend/mvnw.cmd && git commit -m "chore: add maven wrapper 3.9.9"
```

---

## Task 3: 親 POM を作成

**Files:**
- Create: `backend/pom.xml`

- [ ] **Step 1: 親 pom.xml を作成**

ファイル: `c:/work/tool/net/AnomalyDetectionJava/backend/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version>
    <relativePath/>
  </parent>

  <groupId>com.anomalydetection</groupId>
  <artifactId>anomaly-detection-java</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>pom</packaging>
  <name>AnomalyDetection (Java)</name>
  <description>CAN anomaly detection system - Java port</description>

  <properties>
    <java.version>21</java.version>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

    <!-- BOM versions -->
    <spring-modulith.version>1.3.1</spring-modulith.version>

    <!-- Library versions -->
    <archunit.version>1.3.0</archunit.version>
    <mariadb4j.version>3.1.0</mariadb4j.version>
    <mapstruct.version>1.6.2</mapstruct.version>
    <springdoc.version>2.6.0</springdoc.version>
  </properties>

  <modules>
    <module>domain-shared</module>
    <module>domain</module>
    <module>application-contracts</module>
    <module>application</module>
    <module>infrastructure</module>
    <module>web</module>
    <module>auth-server</module>
    <module>db-migrator</module>
    <module>host</module>
  </modules>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-bom</artifactId>
        <version>${spring-modulith.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>

      <!-- Internal modules -->
      <dependency>
        <groupId>com.anomalydetection</groupId>
        <artifactId>anomaly-detection-domain-shared</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>com.anomalydetection</groupId>
        <artifactId>anomaly-detection-domain</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>com.anomalydetection</groupId>
        <artifactId>anomaly-detection-application-contracts</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>com.anomalydetection</groupId>
        <artifactId>anomaly-detection-application</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>com.anomalydetection</groupId>
        <artifactId>anomaly-detection-infrastructure</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>com.anomalydetection</groupId>
        <artifactId>anomaly-detection-web</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>com.anomalydetection</groupId>
        <artifactId>anomaly-detection-auth-server</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>com.anomalydetection</groupId>
        <artifactId>anomaly-detection-db-migrator</artifactId>
        <version>${project.version}</version>
      </dependency>

      <!-- Test libs -->
      <dependency>
        <groupId>com.tngtech.archunit</groupId>
        <artifactId>archunit-junit5</artifactId>
        <version>${archunit.version}</version>
        <scope>test</scope>
      </dependency>
      <dependency>
        <groupId>ch.vorburger.mariaDB4j</groupId>
        <artifactId>mariaDB4j</artifactId>
        <version>${mariadb4j.version}</version>
        <scope>test</scope>
      </dependency>

      <!-- MapStruct (M0 では未使用、後続フェーズで利用) -->
      <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
      </dependency>
      <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
      </dependency>

      <!-- mysql-connector-j は Spring Boot BOM が管理 -->
    </dependencies>
  </dependencyManagement>

  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-compiler-plugin</artifactId>
          <configuration>
            <release>21</release>
            <annotationProcessorPaths>
              <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
              </path>
              <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>${mapstruct.version}</version>
              </path>
            </annotationProcessorPaths>
          </configuration>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
```

- [ ] **Step 2: 親 POM 検証 (まだサブモジュール未作成のため一旦エラーで OK)**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -N validate
```

`-N` (non-recursive) でサブモジュールを無視。期待される出力: `BUILD SUCCESS`

- [ ] **Step 3: コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git add backend/pom.xml && git commit -m "chore: add maven parent pom with dependency management"
```

---

## Task 4: 上流 5 サブモジュール (依存無し or 軽量) の skeleton 作成

`domain-shared`, `domain`, `application-contracts`, `application`, `web`, `auth-server`, `db-migrator` の 7 個。すべて `package-info.java` のみのライブラリモジュール。

**Files:**
- Create: `backend/domain-shared/pom.xml`
- Create: `backend/domain-shared/src/main/java/com/anomalydetection/shared/package-info.java`
- Create: `backend/domain/pom.xml`
- Create: `backend/domain/src/main/java/com/anomalydetection/domain/package-info.java`
- Create: `backend/application-contracts/pom.xml`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/package-info.java`
- Create: `backend/application/pom.xml`
- Create: `backend/application/src/main/java/com/anomalydetection/application/package-info.java`
- Create: `backend/web/pom.xml`
- Create: `backend/web/src/main/java/com/anomalydetection/web/package-info.java`
- Create: `backend/auth-server/pom.xml`
- Create: `backend/auth-server/src/main/java/com/anomalydetection/authserver/package-info.java`
- Create: `backend/db-migrator/pom.xml`
- Create: `backend/db-migrator/src/main/java/com/anomalydetection/dbmigrator/package-info.java`

- [ ] **Step 1: domain-shared モジュールを作成**

ファイル: `backend/domain-shared/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.anomalydetection</groupId>
    <artifactId>anomaly-detection-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>anomaly-detection-domain-shared</artifactId>
  <name>AnomalyDetection :: Domain Shared</name>
  <description>Constants, enums, localization keys, common exceptions (ABP Domain.Shared equivalent)</description>
</project>
```

ファイル: `backend/domain-shared/src/main/java/com/anomalydetection/shared/package-info.java`

```java
/**
 * Shared domain primitives: constants, enums, localization keys, common exceptions.
 *
 * <p>ABP {@code Domain.Shared} 相当。すべての他モジュールから参照可能な最下層。
 */
package com.anomalydetection.shared;
```

- [ ] **Step 2: domain モジュールを作成**

ファイル: `backend/domain/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.anomalydetection</groupId>
    <artifactId>anomaly-detection-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>anomaly-detection-domain</artifactId>
  <name>AnomalyDetection :: Domain</name>
  <description>Aggregates, value objects, domain services, repository interfaces, domain events</description>

  <dependencies>
    <dependency>
      <groupId>com.anomalydetection</groupId>
      <artifactId>anomaly-detection-domain-shared</artifactId>
    </dependency>
  </dependencies>
</project>
```

ファイル: `backend/domain/src/main/java/com/anomalydetection/domain/package-info.java`

```java
/**
 * Domain layer: aggregates, value objects, domain services, repository interfaces, domain events.
 *
 * <p>ABP {@code Domain} 相当。Spring に依存しないピュアな Java で表現する。
 */
package com.anomalydetection.domain;
```

- [ ] **Step 3: application-contracts モジュールを作成**

ファイル: `backend/application-contracts/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.anomalydetection</groupId>
    <artifactId>anomaly-detection-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>anomaly-detection-application-contracts</artifactId>
  <name>AnomalyDetection :: Application Contracts</name>
  <description>DTOs, application service interfaces, permission definitions</description>

  <dependencies>
    <dependency>
      <groupId>com.anomalydetection</groupId>
      <artifactId>anomaly-detection-domain</artifactId>
    </dependency>
  </dependencies>
</project>
```

ファイル: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/package-info.java`

```java
/**
 * Application contracts: DTOs, application service interfaces, permission definitions.
 *
 * <p>ABP {@code Application.Contracts} 相当。フロントエンドや他コンテキストへ公開する API の型を定義する。
 */
package com.anomalydetection.contracts;
```

- [ ] **Step 4: application モジュールを作成**

ファイル: `backend/application/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.anomalydetection</groupId>
    <artifactId>anomaly-detection-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>anomaly-detection-application</artifactId>
  <name>AnomalyDetection :: Application</name>
  <description>Use case implementations / application services (ABP Application equivalent)</description>

  <dependencies>
    <dependency>
      <groupId>com.anomalydetection</groupId>
      <artifactId>anomaly-detection-application-contracts</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-context</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-tx</artifactId>
    </dependency>
  </dependencies>
</project>
```

ファイル: `backend/application/src/main/java/com/anomalydetection/application/package-info.java`

```java
/**
 * Application layer: use case implementations / application services.
 *
 * <p>ABP {@code Application} 相当。{@code @Transactional} 境界を担い、ドメインを協調動作させる。
 */
package com.anomalydetection.application;
```

- [ ] **Step 5: web モジュールを作成**

ファイル: `backend/web/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.anomalydetection</groupId>
    <artifactId>anomaly-detection-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>anomaly-detection-web</artifactId>
  <name>AnomalyDetection :: Web</name>
  <description>REST controllers, exception handlers, OpenAPI configuration, WebSocket (STOMP)</description>

  <dependencies>
    <dependency>
      <groupId>com.anomalydetection</groupId>
      <artifactId>anomaly-detection-application</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
  </dependencies>
</project>
```

ファイル: `backend/web/src/main/java/com/anomalydetection/web/package-info.java`

```java
/**
 * Web layer: REST controllers, exception handlers, OpenAPI configuration, WebSocket (STOMP).
 *
 * <p>ABP {@code HttpApi} 相当。HTTP プロトコル境界。{@code infrastructure} を直接参照してはならない。
 */
package com.anomalydetection.web;
```

- [ ] **Step 6: auth-server モジュールを作成**

ファイル: `backend/auth-server/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.anomalydetection</groupId>
    <artifactId>anomaly-detection-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>anomaly-detection-auth-server</artifactId>
  <name>AnomalyDetection :: Auth Server</name>
  <description>OAuth2/OIDC authorization server (Spring Authorization Server)</description>
</project>
```

ファイル: `backend/auth-server/src/main/java/com/anomalydetection/authserver/package-info.java`

```java
/**
 * OAuth2 / OIDC authorization server endpoints, client registration, JWT key management.
 *
 * <p>ABP の OpenIddict + Identity 相当を Spring Authorization Server で実装する。
 */
package com.anomalydetection.authserver;
```

- [ ] **Step 7: db-migrator モジュールを作成**

ファイル: `backend/db-migrator/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.anomalydetection</groupId>
    <artifactId>anomaly-detection-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>anomaly-detection-db-migrator</artifactId>
  <name>AnomalyDetection :: DB Migrator</name>
  <description>CLI for initial seed data + Liquibase migration runner (ABP DbMigrator equivalent)</description>
</project>
```

ファイル: `backend/db-migrator/src/main/java/com/anomalydetection/dbmigrator/package-info.java`

```java
/**
 * CLI for initial tenant / admin / OAuth2 client / permission seed data.
 *
 * <p>ABP {@code DbMigrator} 相当。後続フェーズ M1〜M4 で本格実装される。
 */
package com.anomalydetection.dbmigrator;
```

- [ ] **Step 8: 7 モジュールのビルド検証**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -B -pl domain-shared,domain,application-contracts,application,web,auth-server,db-migrator -am compile
```

期待される出力: 7 モジュールが `BUILD SUCCESS`

- [ ] **Step 9: コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git add backend/domain-shared backend/domain backend/application-contracts backend/application backend/web backend/auth-server backend/db-migrator && git commit -m "feat(skeleton): add 7 module skeletons (domain-shared, domain, contracts, application, web, auth-server, db-migrator)"
```

---

## Task 5: infrastructure モジュールを Liquibase 空 changelog 付きで作成

**Files:**
- Create: `backend/infrastructure/pom.xml`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/InfrastructureConfiguration.java`
- Create: `backend/infrastructure/src/main/resources/db/changelog/db.changelog-master.yaml`

- [ ] **Step 1: infrastructure pom.xml を作成**

ファイル: `backend/infrastructure/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.anomalydetection</groupId>
    <artifactId>anomaly-detection-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>anomaly-detection-infrastructure</artifactId>
  <name>AnomalyDetection :: Infrastructure</name>
  <description>JPA entities, repository implementations, Liquibase changelogs, multi-tenant filter</description>

  <dependencies>
    <dependency>
      <groupId>com.anomalydetection</groupId>
      <artifactId>anomaly-detection-domain</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.liquibase</groupId>
      <artifactId>liquibase-core</artifactId>
    </dependency>
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>
  </dependencies>
</project>
```

- [ ] **Step 2: 空 Liquibase changelog を作成**

ファイル: `backend/infrastructure/src/main/resources/db/changelog/db.changelog-master.yaml`

```yaml
databaseChangeLog:
  # M0 baseline — empty master. Domain changelogs will be included from M1 onwards.
  - logicalFilePath: db/changelog/db.changelog-master.yaml
```

- [ ] **Step 3: InfrastructureConfiguration を作成**

ファイル: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/InfrastructureConfiguration.java`

```java
package com.anomalydetection.infrastructure;

import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration entry-point for the infrastructure module.
 *
 * <p>当面は空。M1 以降で Hibernate Filter 登録 / TenantConnectionProvider 等を追加。
 */
@Configuration
public class InfrastructureConfiguration {
}
```

- [ ] **Step 4: infrastructure モジュールのコンパイル検証**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -B -pl infrastructure -am compile
```

期待される出力: `BUILD SUCCESS`、`InfrastructureConfiguration.class` が生成

- [ ] **Step 5: コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git add backend/infrastructure && git commit -m "feat(infrastructure): add JPA + Liquibase wiring with empty master changelog"
```

---

## Task 6: host モジュールを Spring Boot 起動可能アプリとして作成

**Files:**
- Create: `backend/host/pom.xml`
- Create: `backend/host/src/main/java/com/anomalydetection/AnomalyDetectionApplication.java`
- Create: `backend/host/src/main/resources/application.yml`
- Create: `backend/host/src/main/resources/application-local.yml`
- Create: `backend/host/src/main/resources/application-test.yml`

- [ ] **Step 1: host pom.xml を作成**

ファイル: `backend/host/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.anomalydetection</groupId>
    <artifactId>anomaly-detection-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>anomaly-detection-host</artifactId>
  <name>AnomalyDetection :: Host</name>
  <description>Spring Boot bootstrap application (ABP HttpApi.Host equivalent)</description>

  <dependencies>
    <!-- All backend modules wired into the runnable jar -->
    <dependency>
      <groupId>com.anomalydetection</groupId>
      <artifactId>anomaly-detection-web</artifactId>
    </dependency>
    <dependency>
      <groupId>com.anomalydetection</groupId>
      <artifactId>anomaly-detection-application</artifactId>
    </dependency>
    <dependency>
      <groupId>com.anomalydetection</groupId>
      <artifactId>anomaly-detection-infrastructure</artifactId>
    </dependency>
    <dependency>
      <groupId>com.anomalydetection</groupId>
      <artifactId>anomaly-detection-auth-server</artifactId>
    </dependency>

    <!-- Spring Boot starters -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Spring Modulith -->
    <dependency>
      <groupId>org.springframework.modulith</groupId>
      <artifactId>spring-modulith-starter-core</artifactId>
    </dependency>

    <!-- Test -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.modulith</groupId>
      <artifactId>spring-modulith-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>com.tngtech.archunit</groupId>
      <artifactId>archunit-junit5</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>ch.vorburger.mariaDB4j</groupId>
      <artifactId>mariaDB4j</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
          <mainClass>com.anomalydetection.AnomalyDetectionApplication</mainClass>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>repackage</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 2: AnomalyDetectionApplication を作成**

ファイル: `backend/host/src/main/java/com/anomalydetection/AnomalyDetectionApplication.java`

```java
package com.anomalydetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * AnomalyDetection (Java port) — application entry point.
 *
 * <p>9 つの Maven サブモジュールに分割された機能を本クラスから組み立てる。
 * Spring Modulith の {@link Modulithic} で {@code com.anomalydetection} 配下を
 * モジュラーモノリスのルートとして宣言する。
 */
@SpringBootApplication
@Modulithic(systemName = "AnomalyDetection")
public class AnomalyDetectionApplication {

  public static void main(String[] args) {
    SpringApplication.run(AnomalyDetectionApplication.class, args);
  }
}
```

- [ ] **Step 3: デフォルト application.yml を作成**

ファイル: `backend/host/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: anomaly-detection
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: false
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml

server:
  port: 44397

management:
  endpoints:
    web:
      exposure:
        include: health, info
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true

logging:
  level:
    root: INFO
    com.anomalydetection: INFO
```

- [ ] **Step 4: application-local.yml を作成 (ローカル MySQL 接続)**

ファイル: `backend/host/src/main/resources/application-local.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/anomaly_detection?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC
    username: root
    password: root
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    com.anomalydetection: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
```

- [ ] **Step 5: application-test.yml を作成 (MariaDB4j 用)**

ファイル: `backend/host/src/main/resources/application-test.yml`

```yaml
# MariaDB4j 用設定。ポートは MariaDB4jExtension が動的に注入する。
spring:
  datasource:
    url: ${TEST_DB_URL:jdbc:mysql://localhost:0/test}
    username: root
    password: ""
  jpa:
    show-sql: false
  liquibase:
    enabled: true

logging:
  level:
    com.anomalydetection: DEBUG
    liquibase: INFO
```

- [ ] **Step 6: フルプロジェクトのコンパイル確認**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -B compile
```

期待される出力: 9 モジュールすべて `BUILD SUCCESS`

- [ ] **Step 7: コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git add backend/host && git commit -m "feat(host): add Spring Boot bootstrap application with Actuator and 3 profiles"
```

---

## Task 7: MariaDB4j テスト用拡張クラスを作成

**Files:**
- Create: `backend/host/src/test/java/com/anomalydetection/host/support/MariaDB4jExtension.java`

- [ ] **Step 1: MariaDB4jExtension を作成**

ファイル: `backend/host/src/test/java/com/anomalydetection/host/support/MariaDB4jExtension.java`

```java
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
              + " @ExtendWith(MariaDB4jExtension.class).");
    }
    registry.add("spring.datasource.url", () -> sharedJdbcUrl);
    registry.add("spring.datasource.username", () -> "root");
    registry.add("spring.datasource.password", () -> "");
  }
}
```

- [ ] **Step 2: コンパイル確認 (テスト本体はまだ存在しない)**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -B -pl host -am test-compile
```

期待される出力: `BUILD SUCCESS`、`MariaDB4jExtension.class` が `host/target/test-classes/` 以下に生成

- [ ] **Step 3: コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git add backend/host/src/test && git commit -m "test(support): add MariaDB4jExtension for Docker-free JPA tests"
```

---

## Task 8: コンテキストロードテストを TDD で追加

**Files:**
- Create: `backend/host/src/test/java/com/anomalydetection/host/AnomalyDetectionApplicationTests.java`

- [ ] **Step 1: 失敗テストを書く**

ファイル: `backend/host/src/test/java/com/anomalydetection/host/AnomalyDetectionApplicationTests.java`

```java
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
```

- [ ] **Step 2: テストを実行して失敗を確認**

実行する前に host モジュールに必要な依存 (`spring-boot-starter-data-jpa` / `mysql-connector-j`) は infrastructure 経由で含まれている。テストを走らせる:

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -B -pl host -am test -Dtest=AnomalyDetectionApplicationTests
```

このタイミングでは **PASS する想定** (Spring Boot のオートコンフィグが MariaDB4j を読み取り、空 changelog を実行して context up に成功するため)。失敗する場合は以下のチェックリストで原因を切り分け:

- `MariaDB4jExtension` が `TEST_DB_URL` システムプロパティをセット済みか (`beforeAll` 順序)
- `application-test.yml` の `${TEST_DB_URL:...}` プレースホルダが解決されているか (起動ログで `spring.datasource.url` を確認)
- Liquibase が空 changelog を `master` として認識しているか (`logicalFilePath` 必須)

期待される出力: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 3: コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git add backend/host/src/test/java/com/anomalydetection/host/AnomalyDetectionApplicationTests.java && git commit -m "test(host): add context-loads smoke test backed by MariaDB4j"
```

---

## Task 9: Actuator health エンドポイントテスト (TDD)

**Files:**
- Create: `backend/host/src/test/java/com/anomalydetection/host/HealthEndpointTest.java`

- [ ] **Step 1: 失敗テストを書く**

ファイル: `backend/host/src/test/java/com/anomalydetection/host/HealthEndpointTest.java`

```java
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
```

- [ ] **Step 2: 失敗を確認**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -B -pl host -am test -Dtest=HealthEndpointTest
```

期待される結果: spring-boot-starter-web が依存に含まれていれば PASS する。Web 関連の依存が不足していて起動できない場合は失敗するので、その場合は `host/pom.xml` に `spring-boot-starter-web` を直接追加する。

- [ ] **Step 3: 必要に応じて web 依存を host/pom.xml に明示追加**

`web` モジュール経由で `spring-boot-starter-web` が推移依存しているはずだが、テストフェーズで認識されない場合は以下を追加:

ファイル: `backend/host/pom.xml` の `<dependencies>` セクション (既存の依存リストの末尾に追記)

```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
```

- [ ] **Step 4: 再テスト**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -B -pl host -am test -Dtest=HealthEndpointTest
```

期待される出力: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 5: コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git add backend/host && git commit -m "test(host): add Actuator health endpoint integration test"
```

---

## Task 10: Liquibase 起動テスト (TDD)

**Files:**
- Create: `backend/host/src/test/java/com/anomalydetection/host/architecture/LiquibaseStartupTest.java`

- [ ] **Step 1: Liquibase の `databasechangelog` テーブルが起動後に存在することを確認するテストを書く**

ファイル: `backend/host/src/test/java/com/anomalydetection/host/architecture/LiquibaseStartupTest.java`

```java
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
```

- [ ] **Step 2: テスト実行**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -B -pl host -am test -Dtest=LiquibaseStartupTest
```

期待される出力: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 3: コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git add backend/host/src/test/java/com/anomalydetection/host/architecture/LiquibaseStartupTest.java && git commit -m "test(infra): verify Liquibase creates bookkeeping tables on startup"
```

---

## Task 11: ArchUnit 4 ルール (TDD で 1 ルールずつ追加)

**Files:**
- Create: `backend/host/src/test/java/com/anomalydetection/host/architecture/ArchitectureTest.java`

すべてのアーキテクチャルールを 1 つのテストクラスに集約。

- [ ] **Step 1: ArchitectureTest を作成 (4 ルール)**

ファイル: `backend/host/src/test/java/com/anomalydetection/host/architecture/ArchitectureTest.java`

```java
package com.anomalydetection.host.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
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
```

- [ ] **Step 2: 実行**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -B -pl host -am test -Dtest=ArchitectureTest
```

期待される出力: `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0` (各 `@ArchTest` フィールドが 1 件のテストとして数えられる)

- [ ] **Step 3: コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git add backend/host/src/test/java/com/anomalydetection/host/architecture/ArchitectureTest.java && git commit -m "test(arch): enforce module boundary rules with ArchUnit (4 rules)"
```

---

## Task 12: Spring Modulith verification テスト

**Files:**
- Create: `backend/host/src/test/java/com/anomalydetection/host/architecture/ModularityTest.java`

- [ ] **Step 1: ModularityTest を作成**

ファイル: `backend/host/src/test/java/com/anomalydetection/host/architecture/ModularityTest.java`

```java
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
```

- [ ] **Step 2: 実行**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -B -pl host -am test -Dtest=ModularityTest
```

期待される出力: `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`、`backend/host/target/spring-modulith-docs/` にドキュメントが生成される

- [ ] **Step 3: 生成ファイルを `.gitignore` から除外確認 (target/ は既に無視されている)**

`backend/host/target/` 配下は `.gitignore` の `target/` で無視されている。明示確認のみ:

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git status backend/host/target/ 2>/dev/null
```

期待される出力: 空 (target/ は untracked にも staged にもならない)

- [ ] **Step 4: コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git add backend/host/src/test/java/com/anomalydetection/host/architecture/ModularityTest.java && git commit -m "test(arch): add Spring Modulith verify and documenter tests"
```

---

## Task 13: フル `mvn verify` と最終確認・タグ付け

**Files:**
- (既存ファイルの確認のみ、新規作成なし)

- [ ] **Step 1: フル verify 実行**

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -B clean verify
```

期待される出力:
- 9 モジュールすべて `BUILD SUCCESS`
- `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0` (4 ArchUnit + 2 Modulith + 1 health + 1 contextLoads + 1 liquibase = 9)
- 最後に `[INFO] Reactor Summary` で 9 モジュール緑

- [ ] **Step 2: ローカル MySQL に DB を作成して手動起動の最終確認**

事前条件: ホストに MySQL 8 がインストールされ、`root` / `root` でログインできる。

```bash
mysql -u root -proot -e "DROP DATABASE IF EXISTS anomaly_detection; CREATE DATABASE anomaly_detection CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
```

```bash
cd c:/work/tool/net/AnomalyDetectionJava/backend && ./mvnw -pl host -am spring-boot:run -Dspring-boot.run.profiles=local
```

別シェルで:

```bash
curl -sS http://localhost:44397/actuator/health
```

期待される出力: `{"status":"UP","groups":["liveness","readiness"]}` または同等の `UP` 応答

確認後、起動シェルで `Ctrl+C` で停止。

- [ ] **Step 3: M0 完了タグを付ける**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git tag -a m0-baseline -m "M0: baseline setup completed (9 modules, Liquibase, Actuator, ArchUnit, Modulith)"
```

確認:

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git tag --list && git log --oneline -20
```

期待される出力: `m0-baseline` タグと M0 で行ったコミット 11 件 (Task 1〜12 ぶん)

- [ ] **Step 4: CLAUDE.md の開発ステータスを更新**

ファイル: `c:/work/tool/net/AnomalyDetectionJava/CLAUDE.md` の「## 5. 開発ステータス」テーブルを更新する。

該当箇所 (旧):

```markdown
| フェーズ | 状態 |
| --- | --- |
| ブレインストーミング (要件・前提整理) | **完了** |
| 設計書 (spec) 執筆 | 進行中 |
| 実装計画 (plan) 作成 | 未着手 |
| 実装 (TDD) | 未着手 |
```

新:

```markdown
| フェーズ | 状態 |
| --- | --- |
| ブレインストーミング (要件・前提整理) | **完了** |
| 設計書 (spec) 執筆 | **完了** |
| 実装計画 (plan) 作成 | **M0 完了** (M1〜M7 は順次) |
| 実装 (TDD) | **M0 (基盤セットアップ) 完了** |
```

- [ ] **Step 5: ステータス更新コミット**

```bash
cd c:/work/tool/net/AnomalyDetectionJava && git add CLAUDE.md && git commit -m "docs: mark M0 baseline setup as complete in CLAUDE.md"
```

---

## Self-Review

### 1. Spec coverage

| Spec の M0 完了条件 | 対応タスク |
| --- | --- |
| Maven 9 モジュール作成 | Task 3, 4, 5, 6 |
| 親 POM (Spring Boot 3.3 BOM, Java 21, MapStruct, Lombok, Spring Modulith BOM) | Task 3 |
| 空 Spring Boot アプリ起動 (`/actuator/health` 返却) | Task 6 (Application + application.yml), Task 9 (test) |
| MySQL 8 接続 + Liquibase 空スキーマ初期化 | Task 5 (changelog), Task 6 (application-local.yml), Task 10 (test) |
| ArchUnit テスト | Task 11 (4 rules) |
| Spring Modulith verify | Task 12 |
| ArchUnit 緑 / verify 緑 | Task 13 |
| Git リポジトリ独立 | Task 1 |

✅ M0 完了条件はすべてカバー。

### 2. Placeholder スキャン

- "TBD" / "TODO" / "implement later" などの曖昧記述: **なし**
- "Add appropriate error handling": **なし**
- "Similar to Task N" のような省略: **なし** (各 pom は完全コードで明示)
- 未定義の関数/型を参照: **なし** (`MariaDB4jExtension.register` は Task 7 で定義、後続テストで利用)

### 3. 型一貫性

- `MariaDB4jExtension.register(DynamicPropertyRegistry)` (Task 7 で定義) は Task 8, 9, 10 でそのままのシグネチャで利用 — 一致
- メインクラス完全修飾名 `com.anomalydetection.AnomalyDetectionApplication` は Task 6 (定義), Task 8/9/10/12 (参照) ですべて同一
- artifactId プレフィックス `anomaly-detection-` は Task 3 (親) → Task 4 (上流 7 モジュール) → Task 5 (infrastructure) → Task 6 (host) で一貫
- Java パッケージ階層 `com.anomalydetection.*` は親 POM〜各サブモジュールで一致

### 4. ハマりどころ補足

- Maven Wrapper 生成 (Task 2 Step 1) はホストに `mvn` が必要。なければ Spring Initializr 等から手動コピー。
- MariaDB4j は初回起動時に MariaDB バイナリを `target/mariadb4j/` に展開するため初回テストは時間がかかる。`.gitignore` で除外済み。
- Spring Modulith `verify()` は Application のパッケージ階層をスキャンするため、`AnomalyDetectionApplication` を `com.anomalydetection` 直下 (= 全サブモジュールの親パッケージ) に配置している (Task 6)。
- ArchUnit ルールはテストパッケージを除外 (`ImportOption.DoNotIncludeTests`) しているため、テストヘルパー (`MariaDB4jExtension` など) はルール対象外。
