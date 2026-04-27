# M3 横断機能 (Cross-Cutting Features) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 横断機能基盤（Permissions / JPA Auditing / Settings / Features / Localization / Audit Logging / Domain Events / Background Jobs / BLOB Storing）をすべて動作する状態で実装する。

**Architecture:** Each feature follows the same layering: domain entity/repository → infrastructure JPA impl → application service/provider. The static-holder pattern (already used for TenantContextHolderProvider) is reused for current-user-id injection in FullAuditedEntity. JWT token customizer is moved from auth-server to infrastructure so it can read permission_grants from DB and embed them as JWT authorities. Spring Security resource server is configured to read `permissions` JWT claim as GrantedAuthority, enabling `@PreAuthorize("hasAuthority('...')")` on all controllers.

**Tech Stack:** Java 21, Spring Boot 3.3, Spring Security, Spring Modulith 1.3.1, Caffeine (Spring Cache), Quartz (RAM store for scaffold), ShedLock 5.x, Spring MessageSource, Liquibase YAML

---

## File Map

### New files — domain-shared
- Create: `backend/domain-shared/src/main/java/com/anomalydetection/shared/CurrentUserIdHolder.java`

### New files — domain
- Create: `backend/domain/src/main/java/com/anomalydetection/domain/permissions/PermissionGrant.java`
- Create: `backend/domain/src/main/java/com/anomalydetection/domain/permissions/PermissionGrantRepository.java`
- Create: `backend/domain/src/main/java/com/anomalydetection/domain/permissions/package-info.java`

### New files — application-contracts
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/permissions/PermissionDefinitionContributor.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/permissions/PermissionDefinitionContext.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/permissions/PermissionGroupDefinition.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/permissions/package-info.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/IdentityPermissions.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/settings/SettingProvider.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/settings/package-info.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/features/FeatureChecker.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/features/package-info.java`

### New files — application
- Create: `backend/application/src/main/java/com/anomalydetection/application/permissions/PermissionManager.java`
- Create: `backend/application/src/main/java/com/anomalydetection/application/permissions/package-info.java`
- Create: `backend/application/src/main/java/com/anomalydetection/application/settings/SettingManager.java`
- Create: `backend/application/src/main/java/com/anomalydetection/application/settings/package-info.java`

### New files — infrastructure
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/SecurityContextUserIdProvider.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/JwtTokenCustomizer.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/JpaPermissionGrantRepository.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/PermissionDefinitionSynchronizer.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/IdentityPermissionDefinitionContributor.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/package-info.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/SettingValueEntity.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/JpaSettingValueRepository.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/DatabaseSettingProvider.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/package-info.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/FeatureValueEntity.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/JpaFeatureValueRepository.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/DatabaseFeatureChecker.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/package-info.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/AuditLogEntity.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/JpaAuditLogRepository.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/AuditLogAspect.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/package-info.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/jobs/QuartzConfiguration.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/jobs/package-info.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/blob/BlobMetadataEntity.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/blob/package-info.java`
- Create: `backend/infrastructure/src/main/resources/db/changelog/004-permissions.yaml`
- Create: `backend/infrastructure/src/main/resources/db/changelog/005-settings.yaml`
- Create: `backend/infrastructure/src/main/resources/db/changelog/006-features.yaml`
- Create: `backend/infrastructure/src/main/resources/db/changelog/007-audit.yaml`
- Create: `backend/infrastructure/src/main/resources/db/changelog/008-modulith-events.yaml`
- Create: `backend/infrastructure/src/main/resources/db/changelog/009-shedlock.yaml`
- Create: `backend/infrastructure/src/main/resources/db/changelog/010-blob.yaml`
- Create: `backend/domain-shared/src/main/resources/i18n/messages.properties`
- Create: `backend/domain-shared/src/main/resources/i18n/messages_ja.properties`
- Create: `backend/domain-shared/src/main/resources/i18n/messages_en.properties`

### Modified files
- Modify: `backend/pom.xml` — add shedlock.version property
- Modify: `backend/domain-shared/pom.xml` — no changes (CurrentUserIdHolder is pure Java)
- Modify: `backend/domain/src/main/java/com/anomalydetection/domain/base/FullAuditedEntity.java` — wire createdBy/lastModifiedBy
- Modify: `backend/infrastructure/pom.xml` — add Caffeine, Quartz, ShedLock dependencies
- Modify: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/InfrastructureConfiguration.java` — add @EnableCaching, locale resolver
- Modify: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/SecurityConfiguration.java` — add JwtAuthenticationConverter with permissions claim
- Modify: `backend/auth-server/src/main/java/com/anomalydetection/authserver/AuthorizationServerConfig.java` — remove tokenCustomizer() (moved to infrastructure)
- Modify: `backend/db-migrator/src/main/java/com/anomalydetection/dbmigrator/SeedDataInitializer.java` — seed identity permissions for admin role
- Modify: `backend/infrastructure/src/main/resources/db/changelog/db.changelog-master.yaml` — include new changelogs
- Modify: `backend/host/pom.xml` — add spring-modulith-starter-jdbc
- Modify: `backend/host/src/main/resources/application.yml` — add cache config, messages config, spring modulith config
- Modify: `backend/host/src/test/java/com/anomalydetection/host/architecture/ArchitectureTest.java` — allow spring-cache in application layer if needed

### New test files
- Create: `backend/host/src/test/java/com/anomalydetection/host/permissions/PermissionCheckTest.java`
- Create: `backend/host/src/test/java/com/anomalydetection/host/settings/SettingProviderTest.java`

---

### Task 1: POM updates — Caffeine, Quartz, ShedLock, Spring Modulith JDBC

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/infrastructure/pom.xml`
- Modify: `backend/host/pom.xml`

- [ ] **Step 1: Add shedlock.version to parent POM properties**

In `backend/pom.xml`, inside `<properties>`:
```xml
<shedlock.version>5.18.0</shedlock.version>
```

- [ ] **Step 2: Add dependencies to infrastructure POM**

In `backend/infrastructure/pom.xml`, add to `<dependencies>`:
```xml
<!-- Caffeine cache -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
  <groupId>com.github.ben-manes.caffeine</groupId>
  <artifactId>caffeine</artifactId>
</dependency>
<!-- Quartz scheduler -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
<!-- ShedLock -->
<dependency>
  <groupId>net.javacrumbs.shedlock</groupId>
  <artifactId>shedlock-spring</artifactId>
  <version>${shedlock.version}</version>
</dependency>
<dependency>
  <groupId>net.javacrumbs.shedlock</groupId>
  <artifactId>shedlock-provider-jdbc-template</artifactId>
  <version>${shedlock.version}</version>
</dependency>
```

- [ ] **Step 3: Add spring-modulith-starter-jdbc to host POM**

In `backend/host/pom.xml`, add to `<dependencies>`:
```xml
<dependency>
  <groupId>org.springframework.modulith</groupId>
  <artifactId>spring-modulith-starter-jdbc</artifactId>
</dependency>
```

- [ ] **Step 4: Build to verify POMs resolve**

```bash
./mvnw dependency:resolve -q 2>&1 | tail -5
```
Expected: no errors

- [ ] **Step 5: Commit**
```bash
git add backend/pom.xml backend/infrastructure/pom.xml backend/host/pom.xml
git commit -m "build(m3): add Caffeine, Quartz, ShedLock, spring-modulith-starter-jdbc"
```

---

### Task 2: Liquibase migrations 004–010

**Files:**
- Create: `backend/infrastructure/src/main/resources/db/changelog/004-permissions.yaml`
- Create: `backend/infrastructure/src/main/resources/db/changelog/005-settings.yaml`
- Create: `backend/infrastructure/src/main/resources/db/changelog/006-features.yaml`
- Create: `backend/infrastructure/src/main/resources/db/changelog/007-audit.yaml`
- Create: `backend/infrastructure/src/main/resources/db/changelog/008-modulith-events.yaml`
- Create: `backend/infrastructure/src/main/resources/db/changelog/009-shedlock.yaml`
- Create: `backend/infrastructure/src/main/resources/db/changelog/010-blob.yaml`
- Modify: `backend/infrastructure/src/main/resources/db/changelog/db.changelog-master.yaml`

- [ ] **Step 1: Create 004-permissions.yaml**

```yaml
databaseChangeLog:
  - logicalFilePath: db/changelog/004-permissions.yaml

  - changeSet:
      id: 004-001-permission-grants
      author: m3-crosscutting
      comment: "Permission grants — stores which role/user holds which named permission"
      changes:
        - createTable:
            tableName: permission_grants
            columns:
              - column:
                  name: id
                  type: VARCHAR(36)
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: name
                  type: VARCHAR(128)
                  constraints:
                    nullable: false
              - column:
                  name: provider_name
                  type: VARCHAR(64)
                  constraints:
                    nullable: false
              - column:
                  name: provider_key
                  type: VARCHAR(64)
              - column:
                  name: tenant_id
                  type: BINARY(16)
        - addUniqueConstraint:
            tableName: permission_grants
            columnNames: name, provider_name, provider_key, tenant_id
            constraintName: uq_permission_grants
```

- [ ] **Step 2: Create 005-settings.yaml**

```yaml
databaseChangeLog:
  - logicalFilePath: db/changelog/005-settings.yaml

  - changeSet:
      id: 005-001-settings
      author: m3-crosscutting
      comment: "Key-value settings with Global/Tenant/User scopes"
      changes:
        - createTable:
            tableName: settings
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: name
                  type: VARCHAR(128)
                  constraints:
                    nullable: false
              - column:
                  name: value
                  type: LONGTEXT
              - column:
                  name: provider_name
                  type: VARCHAR(64)
                  constraints:
                    nullable: false
              - column:
                  name: provider_key
                  type: VARCHAR(64)
        - addUniqueConstraint:
            tableName: settings
            columnNames: name, provider_name, provider_key
            constraintName: uq_settings
```

- [ ] **Step 3: Create 006-features.yaml**

```yaml
databaseChangeLog:
  - logicalFilePath: db/changelog/006-features.yaml

  - changeSet:
      id: 006-001-feature-values
      author: m3-crosscutting
      comment: "Feature flag values per provider (Global/Tenant)"
      changes:
        - createTable:
            tableName: feature_values
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: name
                  type: VARCHAR(128)
                  constraints:
                    nullable: false
              - column:
                  name: value
                  type: VARCHAR(512)
                  constraints:
                    nullable: false
              - column:
                  name: provider_name
                  type: VARCHAR(64)
                  constraints:
                    nullable: false
              - column:
                  name: provider_key
                  type: VARCHAR(64)
        - addUniqueConstraint:
            tableName: feature_values
            columnNames: name, provider_name, provider_key
            constraintName: uq_feature_values
```

- [ ] **Step 4: Create 007-audit.yaml**

```yaml
databaseChangeLog:
  - logicalFilePath: db/changelog/007-audit.yaml

  - changeSet:
      id: 007-001-audit-logs
      author: m3-crosscutting
      comment: "Request-level audit log for API calls"
      changes:
        - createTable:
            tableName: audit_logs
            columns:
              - column:
                  name: id
                  type: BINARY(16)
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: user_id
                  type: BINARY(16)
              - column:
                  name: user_name
                  type: VARCHAR(256)
              - column:
                  name: tenant_id
                  type: BINARY(16)
              - column:
                  name: http_method
                  type: VARCHAR(16)
              - column:
                  name: url
                  type: VARCHAR(2048)
              - column:
                  name: action_name
                  type: VARCHAR(256)
              - column:
                  name: http_status_code
                  type: INT
              - column:
                  name: execution_duration
                  type: BIGINT
              - column:
                  name: occurred_at
                  type: DATETIME(3)
                  constraints:
                    nullable: false
              - column:
                  name: exceptions
                  type: LONGTEXT
```

- [ ] **Step 5: Create 008-modulith-events.yaml**

```yaml
databaseChangeLog:
  - logicalFilePath: db/changelog/008-modulith-events.yaml

  - changeSet:
      id: 008-001-event-publication
      author: m3-crosscutting
      comment: "Spring Modulith event publication table"
      changes:
        - createTable:
            tableName: event_publication
            columns:
              - column:
                  name: id
                  type: VARCHAR(36)
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: listener_id
                  type: VARCHAR(512)
                  constraints:
                    nullable: false
              - column:
                  name: event_type
                  type: VARCHAR(512)
                  constraints:
                    nullable: false
              - column:
                  name: serialized_event
                  type: LONGTEXT
              - column:
                  name: publication_date
                  type: DATETIME(3)
                  constraints:
                    nullable: false
              - column:
                  name: completion_date
                  type: DATETIME(3)
```

- [ ] **Step 6: Create 009-shedlock.yaml**

```yaml
databaseChangeLog:
  - logicalFilePath: db/changelog/009-shedlock.yaml

  - changeSet:
      id: 009-001-shedlock
      author: m3-crosscutting
      comment: "ShedLock distributed lock table"
      changes:
        - createTable:
            tableName: shedlock
            columns:
              - column:
                  name: name
                  type: VARCHAR(64)
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: lock_until
                  type: TIMESTAMP(3)
                  constraints:
                    nullable: false
              - column:
                  name: locked_at
                  type: TIMESTAMP(3)
                  constraints:
                    nullable: false
              - column:
                  name: locked_by
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
```

- [ ] **Step 7: Create 010-blob.yaml**

```yaml
databaseChangeLog:
  - logicalFilePath: db/changelog/010-blob.yaml

  - changeSet:
      id: 010-001-blobs
      author: m3-crosscutting
      comment: "BLOB metadata and content storage"
      changes:
        - createTable:
            tableName: blobs
            columns:
              - column:
                  name: id
                  type: BINARY(16)
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: container_name
                  type: VARCHAR(256)
                  constraints:
                    nullable: false
              - column:
                  name: blob_name
                  type: VARCHAR(1024)
                  constraints:
                    nullable: false
              - column:
                  name: tenant_id
                  type: BINARY(16)
              - column:
                  name: mime_type
                  type: VARCHAR(256)
              - column:
                  name: size_bytes
                  type: BIGINT
              - column:
                  name: content
                  type: LONGBLOB
              - column:
                  name: created_at
                  type: DATETIME(3)
                  constraints:
                    nullable: false
```

- [ ] **Step 8: Update db.changelog-master.yaml**

Add at the end of the `databaseChangeLog` list in `backend/infrastructure/src/main/resources/db/changelog/db.changelog-master.yaml`:
```yaml
  - include:
      file: db/changelog/004-permissions.yaml
  - include:
      file: db/changelog/005-settings.yaml
  - include:
      file: db/changelog/006-features.yaml
  - include:
      file: db/changelog/007-audit.yaml
  - include:
      file: db/changelog/008-modulith-events.yaml
  - include:
      file: db/changelog/009-shedlock.yaml
  - include:
      file: db/changelog/010-blob.yaml
```

- [ ] **Step 9: Verify migrations compile (LiquibaseStartupTest already covers this)**

```bash
./mvnw install -DskipTests -q && ./mvnw test -pl host -Dtest=LiquibaseStartupTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "BUILD|Tests run"
```
Expected: `Tests run: 1, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 10: Commit**
```bash
git add backend/infrastructure/src/main/resources/db/changelog/
git commit -m "feat(m3): add Liquibase migrations 004-010 (permissions, settings, features, audit, events, shedlock, blob)"
```

---

### Task 3: CurrentUserIdHolder + SecurityContextUserIdProvider

These two classes enable `FullAuditedEntity.prePersist()` to populate `createdBy`/`lastModifiedBy` without the `domain` module depending on Spring Security.

**Files:**
- Create: `backend/domain-shared/src/main/java/com/anomalydetection/shared/CurrentUserIdHolder.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/SecurityContextUserIdProvider.java`

- [ ] **Step 1: Write the failing test first**

Create `backend/host/src/test/java/com/anomalydetection/host/audit/AuditingTest.java`:
```java
package com.anomalydetection.host.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anomalydetection.AnomalyDetectionApplication;
import com.anomalydetection.contracts.identity.CreateUserDto;
import com.anomalydetection.domain.identity.UserRepository;
import com.anomalydetection.host.support.MariaDB4jExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class AuditingTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private ObjectMapper objectMapper;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void createdByIsPopulatedFromJwt() throws Exception {
    UUID userId = UUID.randomUUID();
    var dto = new CreateUserDto("testaudit@example.com", "testaudit", "Pass@1234", true);

    mockMvc.perform(post("/api/app/users")
        .with(jwt().jwt(j -> j.subject(userId.toString()).claim("username", "testaudit")))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());

    var saved = userRepository.findByNormalizedUserName("TESTAUDIT");
    assertThat(saved).isPresent();
    assertThat(saved.get().getCreatedBy()).isEqualTo(userId);
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -pl host -Dtest=AuditingTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "ERROR|FAIL|createdBy|Tests run"
```
Expected: test fails because `createdBy` is null.

- [ ] **Step 3: Create CurrentUserIdHolder in domain-shared**

```java
// backend/domain-shared/src/main/java/com/anomalydetection/shared/CurrentUserIdHolder.java
package com.anomalydetection.shared;

import java.util.Optional;
import java.util.UUID;

/**
 * Static bridge that lets domain entities read the current user ID without
 * depending on Spring Security. Infrastructure wires in a provider on startup.
 */
public final class CurrentUserIdHolder {

  private CurrentUserIdHolder() {}

  @FunctionalInterface
  public interface UserIdProvider {
    Optional<UUID> getUserId();
  }

  private static volatile UserIdProvider provider;

  public static void setProvider(UserIdProvider p) {
    provider = p;
  }

  public static Optional<UUID> getUserId() {
    return provider != null ? provider.getUserId() : Optional.empty();
  }
}
```

- [ ] **Step 4: Create SecurityContextUserIdProvider in infrastructure**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/SecurityContextUserIdProvider.java
package com.anomalydetection.infrastructure.security;

import com.anomalydetection.shared.CurrentUserIdHolder;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextUserIdProvider implements CurrentUserIdHolder.UserIdProvider {

  @PostConstruct
  public void register() {
    CurrentUserIdHolder.setProvider(this);
  }

  @Override
  public Optional<UUID> getUserId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      String sub = jwt.getSubject();
      if (sub != null) {
        try {
          return Optional.of(UUID.fromString(sub));
        } catch (IllegalArgumentException ignored) {}
      }
    }
    return Optional.empty();
  }
}
```

- [ ] **Step 5: Commit**
```bash
git add backend/domain-shared/src/main/java/com/anomalydetection/shared/CurrentUserIdHolder.java \
        backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/SecurityContextUserIdProvider.java
git commit -m "feat(m3): add CurrentUserIdHolder + SecurityContextUserIdProvider for JPA auditing"
```

---

### Task 4: Wire createdBy/lastModifiedBy in FullAuditedEntity

**Files:**
- Modify: `backend/domain/src/main/java/com/anomalydetection/domain/base/FullAuditedEntity.java`
- Modify: `backend/domain-shared/pom.xml` — add spring-modulith-core (already done in M2; verify it's there)

- [ ] **Step 1: Update FullAuditedEntity.prePersist/preUpdate**

Replace the two lifecycle methods in `FullAuditedEntity`:
```java
@jakarta.persistence.PrePersist
protected void prePersist() {
  createdAt = Instant.now();
  if (createdBy == null) {
    createdBy = com.anomalydetection.shared.CurrentUserIdHolder.getUserId().orElse(null);
  }
}

@jakarta.persistence.PreUpdate
protected void preUpdate() {
  lastModifiedAt = Instant.now();
  lastModifiedBy = com.anomalydetection.shared.CurrentUserIdHolder.getUserId().orElse(null);
}
```

- [ ] **Step 2: Add domain-shared dependency to domain POM if missing**

Check `backend/domain/pom.xml` — it already has `anomaly-detection-domain-shared`. No change needed.

- [ ] **Step 3: Rebuild and run AuditingTest**

```bash
./mvnw install -DskipTests -q && ./mvnw test -pl host -Dtest=AuditingTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "BUILD|Tests run|createdBy"
```
Expected: `Tests run: 1, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 4: Commit**
```bash
git add backend/domain/src/main/java/com/anomalydetection/domain/base/FullAuditedEntity.java
git commit -m "feat(m3): populate createdBy/lastModifiedBy via CurrentUserIdHolder in FullAuditedEntity"
```

---

### Task 5: Permission domain model

**Files:**
- Create: `backend/domain/src/main/java/com/anomalydetection/domain/permissions/PermissionGrant.java`
- Create: `backend/domain/src/main/java/com/anomalydetection/domain/permissions/PermissionGrantRepository.java`
- Create: `backend/domain/src/main/java/com/anomalydetection/domain/permissions/package-info.java`

- [ ] **Step 1: Create PermissionGrant entity**

```java
// backend/domain/src/main/java/com/anomalydetection/domain/permissions/PermissionGrant.java
package com.anomalydetection.domain.permissions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Records that a provider (role/user/global) holds a named permission.
 * Matches ABP PermissionGrant.
 */
@Entity
@Table(name = "permission_grants")
public class PermissionGrant {

  @Id
  @Column(length = 36)
  private String id;

  @Column(nullable = false, length = 128)
  private String name;

  /** "R" = role, "U" = user, "G" = global */
  @Column(name = "provider_name", nullable = false, length = 64)
  private String providerName;

  /** Role name, user UUID string, or null for global */
  @Column(name = "provider_key", length = 64)
  private String providerKey;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  protected PermissionGrant() {}

  public PermissionGrant(String name, String providerName, String providerKey, UUID tenantId) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
    this.providerName = providerName;
    this.providerKey = providerKey;
    this.tenantId = tenantId;
  }

  public String getId() { return id; }
  public String getName() { return name; }
  public String getProviderName() { return providerName; }
  public String getProviderKey() { return providerKey; }
  public UUID getTenantId() { return tenantId; }
}
```

- [ ] **Step 2: Create PermissionGrantRepository**

```java
// backend/domain/src/main/java/com/anomalydetection/domain/permissions/PermissionGrantRepository.java
package com.anomalydetection.domain.permissions;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionGrantRepository extends JpaRepository<PermissionGrant, String> {

  List<PermissionGrant> findByProviderNameAndProviderKey(String providerName, String providerKey);

  boolean existsByNameAndProviderNameAndProviderKey(
      String name, String providerName, String providerKey);

  List<PermissionGrant> findByProviderNameIn(List<String> providerNames);
}
```

- [ ] **Step 3: Create package-info.java**

```java
// backend/domain/src/main/java/com/anomalydetection/domain/permissions/package-info.java
@NamedInterface("permissions")
package com.anomalydetection.domain.permissions;

import org.springframework.modulith.NamedInterface;
```

- [ ] **Step 4: Commit**
```bash
git add backend/domain/src/main/java/com/anomalydetection/domain/permissions/
git commit -m "feat(m3): add PermissionGrant domain entity and repository"
```

---

### Task 6: Permission contracts (PermissionDefinitionContributor, IdentityPermissions)

**Files:**
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/permissions/PermissionDefinitionContributor.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/permissions/PermissionDefinitionContext.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/permissions/PermissionGroupDefinition.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/permissions/package-info.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/IdentityPermissions.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/settings/SettingProvider.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/settings/package-info.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/features/FeatureChecker.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/features/package-info.java`

- [ ] **Step 1: Create PermissionGroupDefinition**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/permissions/PermissionGroupDefinition.java
package com.anomalydetection.contracts.permissions;

import java.util.ArrayList;
import java.util.List;

public class PermissionGroupDefinition {
  private final String name;
  private final String displayName;
  private final List<String> permissionNames = new ArrayList<>();

  public PermissionGroupDefinition(String name, String displayName) {
    this.name = name;
    this.displayName = displayName;
  }

  public PermissionGroupDefinition addPermission(String permissionName) {
    permissionNames.add(permissionName);
    return this;
  }

  public String getName() { return name; }
  public String getDisplayName() { return displayName; }
  public List<String> getPermissionNames() { return List.copyOf(permissionNames); }
}
```

- [ ] **Step 2: Create PermissionDefinitionContext**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/permissions/PermissionDefinitionContext.java
package com.anomalydetection.contracts.permissions;

import java.util.ArrayList;
import java.util.List;

public class PermissionDefinitionContext {
  private final List<PermissionGroupDefinition> groups = new ArrayList<>();

  public PermissionGroupDefinition addGroup(String name, String displayName) {
    var group = new PermissionGroupDefinition(name, displayName);
    groups.add(group);
    return group;
  }

  public List<String> getAllPermissionNames() {
    return groups.stream()
        .flatMap(g -> g.getPermissionNames().stream())
        .toList();
  }

  public List<PermissionGroupDefinition> getGroups() {
    return List.copyOf(groups);
  }
}
```

- [ ] **Step 3: Create PermissionDefinitionContributor**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/permissions/PermissionDefinitionContributor.java
package com.anomalydetection.contracts.permissions;

/**
 * Implemented by each feature module to declare the permissions it owns.
 * All contributors are collected by PermissionDefinitionSynchronizer at startup.
 */
public interface PermissionDefinitionContributor {
  void define(PermissionDefinitionContext context);
}
```

- [ ] **Step 4: Create package-info.java for permissions**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/permissions/package-info.java
@NamedInterface("permissions")
package com.anomalydetection.contracts.permissions;

import org.springframework.modulith.NamedInterface;
```

- [ ] **Step 5: Create IdentityPermissions constants**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/IdentityPermissions.java
package com.anomalydetection.contracts.identity;

public final class IdentityPermissions {
  public static final String GROUP = "AnomalyDetection.Identity";

  public static final String USERS = GROUP + ".Users";
  public static final String USERS_VIEW = USERS + ".View";
  public static final String USERS_CREATE = USERS + ".Create";
  public static final String USERS_EDIT = USERS + ".Edit";
  public static final String USERS_DELETE = USERS + ".Delete";

  public static final String ROLES = GROUP + ".Roles";
  public static final String ROLES_VIEW = ROLES + ".View";
  public static final String ROLES_CREATE = ROLES + ".Create";
  public static final String ROLES_EDIT = ROLES + ".Edit";
  public static final String ROLES_DELETE = ROLES + ".Delete";

  public static final String TENANTS = GROUP + ".Tenants";
  public static final String TENANTS_VIEW = TENANTS + ".View";
  public static final String TENANTS_CREATE = TENANTS + ".Create";
  public static final String TENANTS_EDIT = TENANTS + ".Edit";
  public static final String TENANTS_DELETE = TENANTS + ".Delete";

  private IdentityPermissions() {}
}
```

- [ ] **Step 6: Create SettingProvider interface**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/settings/SettingProvider.java
package com.anomalydetection.contracts.settings;

import java.util.Optional;

public interface SettingProvider {
  Optional<String> getGlobal(String name);
  Optional<String> getForTenant(String name, String tenantId);
  Optional<String> getForUser(String name, String userId);
  void setGlobal(String name, String value);
  void setForTenant(String name, String value, String tenantId);
  void evictCache(String name, String providerName, String providerKey);
}
```

- [ ] **Step 7: Create SettingProvider package-info**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/settings/package-info.java
@NamedInterface("settings")
package com.anomalydetection.contracts.settings;

import org.springframework.modulith.NamedInterface;
```

- [ ] **Step 8: Create FeatureChecker interface**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/features/FeatureChecker.java
package com.anomalydetection.contracts.features;

public interface FeatureChecker {
  boolean isEnabled(String featureName);
  boolean isEnabledForTenant(String featureName, String tenantId);
}
```

- [ ] **Step 9: Create FeatureChecker package-info**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/features/package-info.java
@NamedInterface("features")
package com.anomalydetection.contracts.features;

import org.springframework.modulith.NamedInterface;
```

- [ ] **Step 10: Commit**
```bash
git add backend/application-contracts/src/main/java/com/anomalydetection/contracts/
git commit -m "feat(m3): add permission, setting, feature contracts (PermissionDefinitionContributor, IdentityPermissions, SettingProvider, FeatureChecker)"
```

---

### Task 7: Permission infrastructure — JPA repo, definition synchronizer, identity contributor

**Files:**
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/JpaPermissionGrantRepository.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/PermissionDefinitionSynchronizer.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/IdentityPermissionDefinitionContributor.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/package-info.java`

- [ ] **Step 1: Create JpaPermissionGrantRepository**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/JpaPermissionGrantRepository.java
package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.domain.permissions.PermissionGrant;
import com.anomalydetection.domain.permissions.PermissionGrantRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPermissionGrantRepository
    extends PermissionGrantRepository, JpaRepository<PermissionGrant, String> {}
```

- [ ] **Step 2: Create IdentityPermissionDefinitionContributor**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/IdentityPermissionDefinitionContributor.java
package com.anomalydetection.infrastructure.permissions;

import com.anomalydetection.contracts.identity.IdentityPermissions;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContributor;
import com.anomalydetection.contracts.permissions.PermissionDefinitionContext;
import org.springframework.stereotype.Component;

@Component
public class IdentityPermissionDefinitionContributor implements PermissionDefinitionContributor {

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(IdentityPermissions.GROUP, "Identity");
    var users = new com.anomalydetection.contracts.permissions.PermissionGroupDefinition(
        IdentityPermissions.USERS, "Users");
    users.addPermission(IdentityPermissions.USERS_VIEW);
    users.addPermission(IdentityPermissions.USERS_CREATE);
    users.addPermission(IdentityPermissions.USERS_EDIT);
    users.addPermission(IdentityPermissions.USERS_DELETE);
    group.addPermission(IdentityPermissions.USERS_VIEW);
    group.addPermission(IdentityPermissions.USERS_CREATE);
    group.addPermission(IdentityPermissions.USERS_EDIT);
    group.addPermission(IdentityPermissions.USERS_DELETE);
    group.addPermission(IdentityPermissions.ROLES_VIEW);
    group.addPermission(IdentityPermissions.ROLES_CREATE);
    group.addPermission(IdentityPermissions.ROLES_EDIT);
    group.addPermission(IdentityPermissions.ROLES_DELETE);
    group.addPermission(IdentityPermissions.TENANTS_VIEW);
    group.addPermission(IdentityPermissions.TENANTS_CREATE);
    group.addPermission(IdentityPermissions.TENANTS_EDIT);
    group.addPermission(IdentityPermissions.TENANTS_DELETE);
  }
}
```

- [ ] **Step 3: Create PermissionDefinitionSynchronizer**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/PermissionDefinitionSynchronizer.java
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

/**
 * Runs at startup to collect all PermissionDefinitionContributor beans and log
 * the full set of defined permissions. (In a later milestone, persists to a
 * permission_definitions table for UI display.)
 */
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
```

- [ ] **Step 4: Create package-info.java**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/package-info.java
@NamedInterface("permissions")
package com.anomalydetection.infrastructure.permissions;

import org.springframework.modulith.NamedInterface;
```

- [ ] **Step 5: Add PermissionManager to application layer**

Create `backend/application/src/main/java/com/anomalydetection/application/permissions/PermissionManager.java`:
```java
package com.anomalydetection.application.permissions;

import com.anomalydetection.domain.permissions.PermissionGrant;
import com.anomalydetection.domain.permissions.PermissionGrantRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PermissionManager {

  private final PermissionGrantRepository repository;

  public PermissionManager(PermissionGrantRepository repository) {
    this.repository = repository;
  }

  public void grantToRole(String permissionName, String roleName, UUID tenantId) {
    if (!repository.existsByNameAndProviderNameAndProviderKey(permissionName, "R", roleName)) {
      repository.save(new PermissionGrant(permissionName, "R", roleName, tenantId));
    }
  }

  public void grantToUser(String permissionName, String userId, UUID tenantId) {
    if (!repository.existsByNameAndProviderNameAndProviderKey(permissionName, "U", userId)) {
      repository.save(new PermissionGrant(permissionName, "U", userId, tenantId));
    }
  }

  @Transactional(readOnly = true)
  public List<String> getPermissionsForRoles(List<String> roleNames) {
    return roleNames.stream()
        .flatMap(role -> repository.findByProviderNameAndProviderKey("R", role).stream())
        .map(PermissionGrant::getName)
        .distinct()
        .toList();
  }
}
```

Create `backend/application/src/main/java/com/anomalydetection/application/permissions/package-info.java`:
```java
@NamedInterface("permissions")
package com.anomalydetection.application.permissions;

import org.springframework.modulith.NamedInterface;
```

- [ ] **Step 6: Commit**
```bash
git add backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/permissions/ \
        backend/application/src/main/java/com/anomalydetection/application/permissions/
git commit -m "feat(m3): add permission infrastructure — JPA repo, definition synchronizer, PermissionManager"
```

---

### Task 8: Move JwtTokenCustomizer to infrastructure (add permissions claim)

The existing `tokenCustomizer()` in `AuthorizationServerConfig` only adds `username`. We move it to `infrastructure/security` so it can inject `PermissionManager` and add permissions to the JWT.

**Files:**
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/JwtTokenCustomizer.java`
- Modify: `backend/auth-server/src/main/java/com/anomalydetection/authserver/AuthorizationServerConfig.java` — remove tokenCustomizer() bean
- Modify: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/SecurityConfiguration.java` — add JwtAuthenticationConverter reading `permissions` claim

- [ ] **Step 1: Create JwtTokenCustomizer in infrastructure**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/JwtTokenCustomizer.java
package com.anomalydetection.infrastructure.security;

import com.anomalydetection.application.permissions.PermissionManager;
import com.anomalydetection.domain.identity.UserRepository;
import java.util.List;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

  private final PermissionManager permissionManager;
  private final UserRepository userRepository;

  public JwtTokenCustomizer(PermissionManager permissionManager, UserRepository userRepository) {
    this.permissionManager = permissionManager;
    this.userRepository = userRepository;
  }

  @Override
  public void customize(JwtEncodingContext context) {
    String principalName = context.getPrincipal().getName();

    if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
      context.getClaims().claim("name", principalName);
    }

    if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
      context.getClaims().claim("username", principalName);

      // Resolve roles for this user (stored as GrantedAuthority names without ROLE_ prefix)
      List<String> roles = context.getPrincipal().getAuthorities().stream()
          .map(a -> a.getAuthority())
          .filter(a -> !a.startsWith("SCOPE_"))
          .toList();

      // Look up user's roles from DB if not in principal authorities
      var userOpt = userRepository.findByNormalizedUserName(principalName.toUpperCase());
      List<String> effectiveRoles = roles.isEmpty() && userOpt.isPresent()
          ? List.of("admin") // fallback: admin user always gets admin role during bootstrap
          : roles;

      List<String> permissions = permissionManager.getPermissionsForRoles(effectiveRoles);
      if (!permissions.isEmpty()) {
        context.getClaims().claim("permissions", permissions);
      }
    }
  }
}
```

- [ ] **Step 2: Remove tokenCustomizer() from AuthorizationServerConfig**

In `backend/auth-server/src/main/java/com/anomalydetection/authserver/AuthorizationServerConfig.java`, delete the entire `tokenCustomizer()` method and its imports:
```java
// DELETE these lines:
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
// ...
@Bean
public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
  return context -> {
    // entire method body deleted
  };
}
```

- [ ] **Step 3: Update SecurityConfiguration to read `permissions` claim as GrantedAuthority**

In `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/SecurityConfiguration.java`, add a `JwtAuthenticationConverter` that maps the `permissions` JWT claim to authorities:

Replace the existing `defaultSecurityFilterChain` method with:
```java
@Bean
@Order(2)
public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
  http
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(
          auth ->
              auth.requestMatchers("/actuator/**").permitAll()
                  .anyRequest().authenticated())
      .formLogin(Customizer.withDefaults())
      .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())))
      .exceptionHandling(
          ex ->
              ex.defaultAuthenticationEntryPointFor(
                  new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                  new AntPathRequestMatcher("/api/**")));
  return http.build();
}

private org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthConverter() {
  var converter = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
  converter.setJwtGrantedAuthoritiesConverter(jwt -> {
    var scopeConverter = new org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationServerConfiguration.AuthorizationServerSettings == null ? new org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter() : new org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter();
    var authorities = new java.util.ArrayList<>(scopeConverter.convert(jwt) != null ? scopeConverter.convert(jwt) : java.util.List.of());
    var permissions = jwt.getClaimAsStringList("permissions");
    if (permissions != null) {
      permissions.stream()
          .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
          .forEach(authorities::add);
    }
    return authorities;
  });
  return converter;
}
```

Wait — that's messy. Let me write this cleanly with proper imports. Replace the entire `SecurityConfiguration.java` content:

```java
package com.anomalydetection.infrastructure.security;

import java.util.ArrayList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

  @Bean
  @Order(2)
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated())
        .formLogin(Customizer.withDefaults())
        .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())))
        .exceptionHandling(
            ex ->
                ex.defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")));
    return http.build();
  }

  private JwtAuthenticationConverter jwtAuthConverter() {
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
      var scopeConverter = new JwtGrantedAuthoritiesConverter();
      var authorities = new ArrayList<>(scopeConverter.convert(jwt) != null
          ? scopeConverter.convert(jwt) : java.util.List.of());
      var permissions = jwt.getClaimAsStringList("permissions");
      if (permissions != null) {
        permissions.stream()
            .map(SimpleGrantedAuthority::new)
            .forEach(authorities::add);
      }
      return authorities;
    });
    return converter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
```

- [ ] **Step 4: Build and run existing auth tests to verify no regressions**

```bash
./mvnw install -DskipTests -q && ./mvnw test -pl host -Dtest="OAuth2EndpointsTest,ResourceServerProtectionTest" -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "BUILD|Tests run"
```
Expected: `Tests run: 5, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 5: Commit**
```bash
git add backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/ \
        backend/auth-server/src/main/java/com/anomalydetection/authserver/AuthorizationServerConfig.java
git commit -m "feat(m3): move JWT token customizer to infrastructure, add permissions claim"
```

---

### Task 9: Settings — SettingValueEntity + DatabaseSettingProvider with Caffeine cache

**Files:**
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/SettingValueEntity.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/JpaSettingValueRepository.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/DatabaseSettingProvider.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/package-info.java`
- Modify: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/InfrastructureConfiguration.java` — add @EnableCaching

- [ ] **Step 1: Write failing test**

Create `backend/host/src/test/java/com/anomalydetection/host/settings/SettingProviderTest.java`:
```java
package com.anomalydetection.host.settings;

import static org.assertj.core.api.Assertions.assertThat;

import com.anomalydetection.AnomalyDetectionApplication;
import com.anomalydetection.contracts.settings.SettingProvider;
import com.anomalydetection.host.support.MariaDB4jExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(classes = AnomalyDetectionApplication.class)
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class SettingProviderTest {

  @Autowired private SettingProvider settingProvider;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void setAndGetGlobalSetting() {
    settingProvider.setGlobal("TestSetting.Color", "blue");
    assertThat(settingProvider.getGlobal("TestSetting.Color")).contains("blue");
  }

  @Test
  void missingSettingReturnsEmpty() {
    assertThat(settingProvider.getGlobal("NoSuchSetting")).isEmpty();
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw install -DskipTests -q && ./mvnw test -pl host -Dtest=SettingProviderTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "ERROR|FAIL|Tests run" | head -5
```
Expected: FAIL (SettingProvider bean not found).

- [ ] **Step 3: Create SettingValueEntity**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/SettingValueEntity.java
package com.anomalydetection.infrastructure.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "settings")
public class SettingValueEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 128)
  private String name;

  @Column(columnDefinition = "LONGTEXT")
  private String value;

  @Column(name = "provider_name", nullable = false, length = 64)
  private String providerName;

  @Column(name = "provider_key", length = 64)
  private String providerKey;

  protected SettingValueEntity() {}

  public SettingValueEntity(String name, String value, String providerName, String providerKey) {
    this.name = name;
    this.value = value;
    this.providerName = providerName;
    this.providerKey = providerKey;
  }

  public Long getId() { return id; }
  public String getName() { return name; }
  public String getValue() { return value; }
  public void setValue(String value) { this.value = value; }
  public String getProviderName() { return providerName; }
  public String getProviderKey() { return providerKey; }
}
```

- [ ] **Step 4: Create JpaSettingValueRepository**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/JpaSettingValueRepository.java
package com.anomalydetection.infrastructure.settings;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaSettingValueRepository extends JpaRepository<SettingValueEntity, Long> {
  Optional<SettingValueEntity> findByNameAndProviderNameAndProviderKey(
      String name, String providerName, String providerKey);
}
```

- [ ] **Step 5: Create DatabaseSettingProvider**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/DatabaseSettingProvider.java
package com.anomalydetection.infrastructure.settings;

import com.anomalydetection.contracts.settings.SettingProvider;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DatabaseSettingProvider implements SettingProvider {

  private final JpaSettingValueRepository repository;

  public DatabaseSettingProvider(JpaSettingValueRepository repository) {
    this.repository = repository;
  }

  @Override
  @Cacheable(value = "settings", key = "'G:' + #name")
  @Transactional(readOnly = true)
  public Optional<String> getGlobal(String name) {
    return repository.findByNameAndProviderNameAndProviderKey(name, "G", null)
        .map(SettingValueEntity::getValue);
  }

  @Override
  @Cacheable(value = "settings", key = "'T:' + #tenantId + ':' + #name")
  @Transactional(readOnly = true)
  public Optional<String> getForTenant(String name, String tenantId) {
    return repository.findByNameAndProviderNameAndProviderKey(name, "T", tenantId)
        .map(SettingValueEntity::getValue);
  }

  @Override
  @Cacheable(value = "settings", key = "'U:' + #userId + ':' + #name")
  @Transactional(readOnly = true)
  public Optional<String> getForUser(String name, String userId) {
    return repository.findByNameAndProviderNameAndProviderKey(name, "U", userId)
        .map(SettingValueEntity::getValue);
  }

  @Override
  @CacheEvict(value = "settings", key = "'G:' + #name")
  public void setGlobal(String name, String value) {
    upsert(name, value, "G", null);
  }

  @Override
  @CacheEvict(value = "settings", key = "'T:' + #tenantId + ':' + #name")
  public void setForTenant(String name, String value, String tenantId) {
    upsert(name, value, "T", tenantId);
  }

  @Override
  @CacheEvict(value = "settings", key = "#name")
  public void evictCache(String name, String providerName, String providerKey) {
    // cache eviction only
  }

  private void upsert(String name, String value, String providerName, String providerKey) {
    var existing = repository.findByNameAndProviderNameAndProviderKey(name, providerName, providerKey);
    if (existing.isPresent()) {
      existing.get().setValue(value);
      repository.save(existing.get());
    } else {
      repository.save(new SettingValueEntity(name, value, providerName, providerKey));
    }
  }
}
```

- [ ] **Step 6: Create package-info.java**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/package-info.java
@NamedInterface("settings")
package com.anomalydetection.infrastructure.settings;

import org.springframework.modulith.NamedInterface;
```

- [ ] **Step 7: Add @EnableCaching to InfrastructureConfiguration**

Add `@EnableCaching` annotation to `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/InfrastructureConfiguration.java`:
```java
import org.springframework.cache.annotation.EnableCaching;

@Configuration
@EnableCaching
@EnableJpaRepositories(basePackages = "com.anomalydetection.infrastructure")
public class InfrastructureConfiguration {
  // ... existing content unchanged
}
```

- [ ] **Step 8: Add cache configuration to application.yml**

In `backend/host/src/main/resources/application.yml`, add:
```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: expireAfterWrite=5m,maximumSize=1000
  messages:
    basename: i18n/messages
    encoding: UTF-8
```

- [ ] **Step 9: Build and run SettingProviderTest**

```bash
./mvnw install -DskipTests -q && ./mvnw test -pl host -Dtest=SettingProviderTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "BUILD|Tests run"
```
Expected: `Tests run: 2, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 10: Commit**
```bash
git add backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/settings/ \
        backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/InfrastructureConfiguration.java \
        backend/host/src/main/resources/application.yml
git commit -m "feat(m3): add Settings — SettingValueEntity, DatabaseSettingProvider with Caffeine cache"
```

---

### Task 10: Features — FeatureValueEntity + DatabaseFeatureChecker with Caffeine

**Files:**
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/FeatureValueEntity.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/JpaFeatureValueRepository.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/DatabaseFeatureChecker.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/package-info.java`

- [ ] **Step 1: Create FeatureValueEntity**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/FeatureValueEntity.java
package com.anomalydetection.infrastructure.features;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "feature_values")
public class FeatureValueEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 128)
  private String name;

  @Column(nullable = false, length = 512)
  private String value;

  @Column(name = "provider_name", nullable = false, length = 64)
  private String providerName;

  @Column(name = "provider_key", length = 64)
  private String providerKey;

  protected FeatureValueEntity() {}

  public FeatureValueEntity(String name, String value, String providerName, String providerKey) {
    this.name = name;
    this.value = value;
    this.providerName = providerName;
    this.providerKey = providerKey;
  }

  public Long getId() { return id; }
  public String getName() { return name; }
  public String getValue() { return value; }
  public String getProviderName() { return providerName; }
  public String getProviderKey() { return providerKey; }
}
```

- [ ] **Step 2: Create JpaFeatureValueRepository**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/JpaFeatureValueRepository.java
package com.anomalydetection.infrastructure.features;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaFeatureValueRepository extends JpaRepository<FeatureValueEntity, Long> {
  Optional<FeatureValueEntity> findByNameAndProviderNameAndProviderKey(
      String name, String providerName, String providerKey);
}
```

- [ ] **Step 3: Create DatabaseFeatureChecker**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/DatabaseFeatureChecker.java
package com.anomalydetection.infrastructure.features;

import com.anomalydetection.contracts.features.FeatureChecker;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DatabaseFeatureChecker implements FeatureChecker {

  private final JpaFeatureValueRepository repository;

  public DatabaseFeatureChecker(JpaFeatureValueRepository repository) {
    this.repository = repository;
  }

  @Override
  @Cacheable(value = "features", key = "'G:' + #featureName")
  public boolean isEnabled(String featureName) {
    return repository.findByNameAndProviderNameAndProviderKey(featureName, "G", null)
        .map(f -> Boolean.parseBoolean(f.getValue()))
        .orElse(false);
  }

  @Override
  @Cacheable(value = "features", key = "'T:' + #tenantId + ':' + #featureName")
  public boolean isEnabledForTenant(String featureName, String tenantId) {
    // Tenant override first, fall back to global
    Optional<FeatureValueEntity> tenantValue = repository
        .findByNameAndProviderNameAndProviderKey(featureName, "T", tenantId);
    if (tenantValue.isPresent()) {
      return Boolean.parseBoolean(tenantValue.get().getValue());
    }
    return isEnabled(featureName);
  }
}
```

- [ ] **Step 4: Create package-info.java**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/package-info.java
@NamedInterface("features")
package com.anomalydetection.infrastructure.features;

import org.springframework.modulith.NamedInterface;
```

- [ ] **Step 5: Commit**
```bash
git add backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/features/
git commit -m "feat(m3): add Features — FeatureValueEntity, DatabaseFeatureChecker with Caffeine cache"
```

---

### Task 11: Audit logging — AuditLog entity + AOP aspect

**Files:**
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/AuditLogEntity.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/JpaAuditLogRepository.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/AuditLogAspect.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/package-info.java`

- [ ] **Step 1: Create AuditLogEntity**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/AuditLogEntity.java
package com.anomalydetection.infrastructure.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "user_id", columnDefinition = "BINARY(16)")
  private UUID userId;

  @Column(name = "user_name", length = 256)
  private String userName;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "http_method", length = 16)
  private String httpMethod;

  @Column(length = 2048)
  private String url;

  @Column(name = "action_name", length = 256)
  private String actionName;

  @Column(name = "http_status_code")
  private Integer httpStatusCode;

  @Column(name = "execution_duration")
  private Long executionDuration;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(columnDefinition = "LONGTEXT")
  private String exceptions;

  protected AuditLogEntity() {}

  public AuditLogEntity(UUID userId, String userName, UUID tenantId,
      String httpMethod, String url, String actionName, Instant occurredAt) {
    this.id = UUID.randomUUID();
    this.userId = userId;
    this.userName = userName;
    this.tenantId = tenantId;
    this.httpMethod = httpMethod;
    this.url = url;
    this.actionName = actionName;
    this.occurredAt = occurredAt;
  }

  public UUID getId() { return id; }
  public void setHttpStatusCode(Integer code) { this.httpStatusCode = code; }
  public void setExecutionDuration(Long ms) { this.executionDuration = ms; }
  public void setExceptions(String ex) { this.exceptions = ex; }
}
```

- [ ] **Step 2: Create JpaAuditLogRepository**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/JpaAuditLogRepository.java
package com.anomalydetection.infrastructure.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

interface JpaAuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {}
```

- [ ] **Step 3: Create AuditLogAspect**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/AuditLogAspect.java
package com.anomalydetection.infrastructure.audit;

import com.anomalydetection.infrastructure.multitenancy.CurrentTenantHolder;
import com.anomalydetection.shared.CurrentUserIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditLogAspect {

  private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

  private final JpaAuditLogRepository repository;
  private final CurrentTenantHolder currentTenantHolder;

  public AuditLogAspect(JpaAuditLogRepository repository, CurrentTenantHolder currentTenantHolder) {
    this.repository = repository;
    this.currentTenantHolder = currentTenantHolder;
  }

  @Around("within(@org.springframework.web.bind.annotation.RestController *)")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Object audit(ProceedingJoinPoint pjp) throws Throwable {
    var start = Instant.now();
    var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest req = attrs != null ? attrs.getRequest() : null;

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UUID userId = CurrentUserIdHolder.getUserId().orElse(null);
    String userName = auth != null ? auth.getName() : null;
    UUID tenantId = currentTenantHolder.getTenantId().orElse(null);

    String method = req != null ? req.getMethod() : null;
    String url = req != null ? req.getRequestURI() : null;
    String actionName = pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();

    AuditLogEntity entry = new AuditLogEntity(userId, userName, tenantId, method, url, actionName, start);

    Throwable thrown = null;
    try {
      Object result = pjp.proceed();
      return result;
    } catch (Throwable t) {
      thrown = t;
      entry.setExceptions(t.getClass().getName() + ": " + t.getMessage());
      throw t;
    } finally {
      long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
      entry.setExecutionDuration(duration);
      try {
        repository.save(entry);
      } catch (Exception ex) {
        log.warn("Failed to save audit log entry", ex);
      }
    }
  }
}
```

- [ ] **Step 4: Create package-info.java**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/package-info.java
@NamedInterface("audit")
package com.anomalydetection.infrastructure.audit;

import org.springframework.modulith.NamedInterface;
```

- [ ] **Step 5: Commit**
```bash
git add backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/audit/
git commit -m "feat(m3): add audit logging — AuditLogEntity and AuditLogAspect (AOP on RestControllers)"
```

---

### Task 12: Localization — MessageSource + LocaleResolver + message properties

**Files:**
- Create: `backend/domain-shared/src/main/resources/i18n/messages.properties`
- Create: `backend/domain-shared/src/main/resources/i18n/messages_ja.properties`
- Create: `backend/domain-shared/src/main/resources/i18n/messages_en.properties`
- Modify: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/InfrastructureConfiguration.java` — add LocaleResolver bean

- [ ] **Step 1: Create message properties files**

`backend/domain-shared/src/main/resources/i18n/messages.properties` (fallback — English):
```properties
error.notFound=Resource not found.
error.unauthorized=Authentication required.
error.forbidden=Access denied.
error.validation=Validation failed: {0}
identity.users.notFound=User not found: {0}
identity.roles.notFound=Role not found: {0}
multitenancy.tenants.notFound=Tenant not found: {0}
```

`backend/domain-shared/src/main/resources/i18n/messages_en.properties`:
```properties
error.notFound=Resource not found.
error.unauthorized=Authentication required.
error.forbidden=Access denied.
error.validation=Validation failed: {0}
identity.users.notFound=User not found: {0}
identity.roles.notFound=Role not found: {0}
multitenancy.tenants.notFound=Tenant not found: {0}
```

`backend/domain-shared/src/main/resources/i18n/messages_ja.properties`:
```properties
error.notFound=リソースが見つかりません。
error.unauthorized=認証が必要です。
error.forbidden=アクセスが拒否されました。
error.validation=バリデーションエラー: {0}
identity.users.notFound=ユーザーが見つかりません: {0}
identity.roles.notFound=ロールが見つかりません: {0}
multitenancy.tenants.notFound=テナントが見つかりません: {0}
```

- [ ] **Step 2: Add LocaleResolver bean to InfrastructureConfiguration**

Add to `InfrastructureConfiguration.java`:
```java
import java.util.List;
import java.util.Locale;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

// inside InfrastructureConfiguration class:
@Bean
public LocaleResolver localeResolver() {
  var resolver = new AcceptHeaderLocaleResolver();
  resolver.setSupportedLocales(List.of(Locale.ENGLISH, Locale.JAPANESE));
  resolver.setDefaultLocale(Locale.ENGLISH);
  return resolver;
}
```

- [ ] **Step 3: Verify spring.messages config in application.yml (already added in Task 9)**

`backend/host/src/main/resources/application.yml` should already have:
```yaml
spring:
  messages:
    basename: i18n/messages
    encoding: UTF-8
```

- [ ] **Step 4: Commit**
```bash
git add backend/domain-shared/src/main/resources/i18n/ \
        backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/InfrastructureConfiguration.java
git commit -m "feat(m3): add localization — MessageSource with ja/en message properties, AcceptHeaderLocaleResolver"
```

---

### Task 13: Background Jobs scaffold — Quartz (RAM store) + ShedLock

**Files:**
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/jobs/QuartzConfiguration.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/jobs/package-info.java`
- Modify: `backend/host/src/main/resources/application.yml` — add Quartz config

- [ ] **Step 1: Create QuartzConfiguration**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/jobs/QuartzConfiguration.java
package com.anomalydetection.infrastructure.jobs;

import java.time.Duration;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class QuartzConfiguration {

  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(
        JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .usingDbTime()
            .build()
    );
  }
}
```

- [ ] **Step 2: Add Quartz RAM-store config to application.yml**

In `backend/host/src/main/resources/application.yml`, add:
```yaml
spring:
  quartz:
    job-store-type: memory
    properties:
      org.quartz.scheduler.instanceName: AnomalyDetectionScheduler
      org.quartz.scheduler.instanceId: AUTO
      org.quartz.threadPool.threadCount: 5
```

- [ ] **Step 3: Create package-info.java**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/jobs/package-info.java
@NamedInterface("jobs")
package com.anomalydetection.infrastructure.jobs;

import org.springframework.modulith.NamedInterface;
```

- [ ] **Step 4: Commit**
```bash
git add backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/jobs/ \
        backend/host/src/main/resources/application.yml
git commit -m "feat(m3): add background jobs scaffold — Quartz (RAM store) + ShedLock"
```

---

### Task 14: BLOB storing scaffold

**Files:**
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/blob/BlobMetadataEntity.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/blob/package-info.java`

- [ ] **Step 1: Create BlobMetadataEntity**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/blob/BlobMetadataEntity.java
package com.anomalydetection.infrastructure.blob;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blobs")
public class BlobMetadataEntity {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "container_name", nullable = false, length = 256)
  private String containerName;

  @Column(name = "blob_name", nullable = false, length = 1024)
  private String blobName;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "mime_type", length = 256)
  private String mimeType;

  @Column(name = "size_bytes")
  private Long sizeBytes;

  @Lob
  @Column(columnDefinition = "LONGBLOB")
  private byte[] content;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected BlobMetadataEntity() {}

  public BlobMetadataEntity(String containerName, String blobName, UUID tenantId,
      String mimeType, byte[] content) {
    this.id = UUID.randomUUID();
    this.containerName = containerName;
    this.blobName = blobName;
    this.tenantId = tenantId;
    this.mimeType = mimeType;
    this.content = content;
    this.sizeBytes = content != null ? (long) content.length : 0L;
    this.createdAt = Instant.now();
  }

  public UUID getId() { return id; }
  public String getContainerName() { return containerName; }
  public String getBlobName() { return blobName; }
  public UUID getTenantId() { return tenantId; }
  public String getMimeType() { return mimeType; }
  public Long getSizeBytes() { return sizeBytes; }
  public byte[] getContent() { return content; }
  public Instant getCreatedAt() { return createdAt; }
}
```

- [ ] **Step 2: Create package-info.java**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/blob/package-info.java
@NamedInterface("blob")
package com.anomalydetection.infrastructure.blob;

import org.springframework.modulith.NamedInterface;
```

- [ ] **Step 3: Commit**
```bash
git add backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/blob/
git commit -m "feat(m3): add BLOB storing scaffold — BlobMetadataEntity"
```

---

### Task 15: SeedDataInitializer — grant all identity permissions to admin role

**Files:**
- Modify: `backend/db-migrator/src/main/java/com/anomalydetection/dbmigrator/SeedDataInitializer.java`
- Modify: `backend/db-migrator/pom.xml` — add anomaly-detection-application dependency

- [ ] **Step 1: Add application dependency to db-migrator POM**

Check `backend/db-migrator/pom.xml`. Add if missing:
```xml
<dependency>
  <groupId>com.anomalydetection</groupId>
  <artifactId>anomaly-detection-application</artifactId>
</dependency>
```

- [ ] **Step 2: Update SeedDataInitializer to seed permissions**

Add a `seedAdminRolePermissions()` method and call it from `run()`. Here is the full updated `run()` and new method (add to existing class):

```java
// Add field:
private final PermissionManager permissionManager;

// Update constructor to add PermissionManager parameter:
public SeedDataInitializer(
    TenantRepository tenantRepository,
    UserRepository userRepository,
    RoleRepository roleRepository,
    PasswordEncoder passwordEncoder,
    JdbcTemplate jdbcTemplate,
    com.anomalydetection.application.permissions.PermissionManager permissionManager) {
  this.tenantRepository = tenantRepository;
  this.userRepository = userRepository;
  this.roleRepository = roleRepository;
  this.passwordEncoder = passwordEncoder;
  this.jdbcTemplate = jdbcTemplate;
  this.permissionManager = permissionManager;
}

// Update run():
@Override
@Transactional
public void run(ApplicationArguments args) {
  seedDefaultTenant();
  seedAdminRole();
  seedAdminUser();
  seedOAuth2SpaClient();
  seedAdminRolePermissions();
}

// Add new method:
private void seedAdminRolePermissions() {
  var allPermissions = java.util.List.of(
      com.anomalydetection.contracts.identity.IdentityPermissions.USERS_VIEW,
      com.anomalydetection.contracts.identity.IdentityPermissions.USERS_CREATE,
      com.anomalydetection.contracts.identity.IdentityPermissions.USERS_EDIT,
      com.anomalydetection.contracts.identity.IdentityPermissions.USERS_DELETE,
      com.anomalydetection.contracts.identity.IdentityPermissions.ROLES_VIEW,
      com.anomalydetection.contracts.identity.IdentityPermissions.ROLES_CREATE,
      com.anomalydetection.contracts.identity.IdentityPermissions.ROLES_EDIT,
      com.anomalydetection.contracts.identity.IdentityPermissions.ROLES_DELETE,
      com.anomalydetection.contracts.identity.IdentityPermissions.TENANTS_VIEW,
      com.anomalydetection.contracts.identity.IdentityPermissions.TENANTS_CREATE,
      com.anomalydetection.contracts.identity.IdentityPermissions.TENANTS_EDIT,
      com.anomalydetection.contracts.identity.IdentityPermissions.TENANTS_DELETE
  );
  for (String perm : allPermissions) {
    permissionManager.grantToRole(perm, ADMIN_ROLE_NAME, null); // null tenantId = host-level
  }
  log.info("Seeded {} permissions for admin role", allPermissions.size());
}
```

- [ ] **Step 3: Commit**
```bash
git add backend/db-migrator/
git commit -m "feat(m3): seed all identity permissions for admin role in SeedDataInitializer"
```

---

### Task 16: Permission-protected endpoint test

**Files:**
- Create: `backend/host/src/test/java/com/anomalydetection/host/permissions/PermissionCheckTest.java`

- [ ] **Step 1: Write the test**

```java
package com.anomalydetection.host.permissions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anomalydetection.AnomalyDetectionApplication;
import com.anomalydetection.contracts.identity.IdentityPermissions;
import com.anomalydetection.host.support.MariaDB4jExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class PermissionCheckTest {

  @Autowired private MockMvc mockMvc;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void getUsers_returns403_whenPermissionMissing() throws Exception {
    mockMvc.perform(get("/api/app/users").with(jwt()))
        .andExpect(status().isForbidden());
  }

  @Test
  void getUsers_returns200_whenPermissionPresent() throws Exception {
    mockMvc.perform(get("/api/app/users")
        .with(jwt().authorities(new SimpleGrantedAuthority(IdentityPermissions.USERS_VIEW))))
        .andExpect(status().isOk());
  }
}
```

**Note:** This test requires `UsersController.getList()` to be annotated with `@PreAuthorize`. Add annotation in Step 2.

- [ ] **Step 2: Add @PreAuthorize to UsersController**

In `backend/web/src/main/java/com/anomalydetection/web/identity/UsersController.java`, add import and annotation to `getList()`:
```java
import org.springframework.security.access.prepost.PreAuthorize;

@GetMapping
@PreAuthorize("hasAuthority('" + com.anomalydetection.contracts.identity.IdentityPermissions.USERS_VIEW + "')")
public PagedResultDto<UserDto> getList(...) { ... }
```

- [ ] **Step 3: Build, install, run test**

```bash
./mvnw install -DskipTests -q && ./mvnw test -pl host -Dtest=PermissionCheckTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "BUILD|Tests run"
```
Expected: `Tests run: 2, Failures: 0, Errors: 0` and `BUILD SUCCESS`

- [ ] **Step 4: Commit**
```bash
git add backend/host/src/test/java/com/anomalydetection/host/permissions/ \
        backend/web/src/main/java/com/anomalydetection/web/identity/UsersController.java
git commit -m "test(m3): add PermissionCheckTest — verify @PreAuthorize blocks/allows based on JWT authority"
```

---

### Task 17: @NamedInterface package-info.java for all new sub-packages

Missing `package-info.java` files will cause `ModularityTest` to fail.

**Files:** Add `package-info.java` to every new sub-package that was created.

- [ ] **Step 1: Add missing package-info files**

The following packages need `@NamedInterface` package-info.java files (already done inline in tasks above, but verify each one exists):

- `domain-shared/shared` — already has `package-info.java` from M0, no sub-packages added
- `domain/permissions` — created in Task 5
- `application-contracts/permissions` — created in Task 6
- `application-contracts/settings` — created in Task 6
- `application-contracts/features` — created in Task 6
- `application/permissions` — created in Task 7
- `application/settings` — need to create below
- `infrastructure/permissions` — created in Task 7
- `infrastructure/settings` — created in Task 9
- `infrastructure/features` — created in Task 10
- `infrastructure/audit` — created in Task 11
- `infrastructure/jobs` — created in Task 13
- `infrastructure/blob` — created in Task 14

Create missing ones:

`backend/application/src/main/java/com/anomalydetection/application/settings/package-info.java`:
```java
@NamedInterface("settings")
package com.anomalydetection.application.settings;

import org.springframework.modulith.NamedInterface;
```

Also add `SettingManager` to `application/settings/` package:
```java
// backend/application/src/main/java/com/anomalydetection/application/settings/SettingManager.java
package com.anomalydetection.application.settings;

import com.anomalydetection.contracts.settings.SettingProvider;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SettingManager {

  private final SettingProvider settingProvider;

  public SettingManager(SettingProvider settingProvider) {
    this.settingProvider = settingProvider;
  }

  public Optional<String> getGlobal(String name) {
    return settingProvider.getGlobal(name);
  }

  public void setGlobal(String name, String value) {
    settingProvider.setGlobal(name, value);
  }
}
```

- [ ] **Step 2: Rebuild and run ModularityTest**

```bash
./mvnw install -DskipTests -q && ./mvnw test -pl host -Dtest=ModularityTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1 | grep -E "BUILD|Tests run|Violations"
```
Expected: `Tests run: 2, Failures: 0, Errors: 0` and `BUILD SUCCESS`

If violations remain, add the missing `@NamedInterface` to reported package.

- [ ] **Step 3: Commit**
```bash
git add backend/application/src/main/java/com/anomalydetection/application/settings/
git commit -m "feat(m3): add SettingManager + package-info.java for all new sub-packages"
```

---

### Task 18: Full build verification + git tag

- [ ] **Step 1: Run full mvn verify**

```bash
./mvnw verify 2>&1 | tail -20
```
Expected: all modules SUCCESS, all tests green.

If any test fails, diagnose and fix before proceeding.

- [ ] **Step 2: Check test count**

```bash
./mvnw test -pl host 2>&1 | grep "Tests run:" | tail -3
```
Expected: final line shows all tests passing (no failures).

- [ ] **Step 3: Update CLAUDE.md milestone table**

In `CLAUDE.md`, update:
```
| **M3** (横断機能: Permissions / Settings / Features / Audit / Jobs / BLOB / i18n) | **完了** (tag: `m3-cross-cutting`) |
```
And update 5.3 note to say "次は **M4 (コアドメイン移植)** に進む。"

- [ ] **Step 4: Commit and tag**
```bash
git add CLAUDE.md
git commit -m "docs: mark M3 complete"
git tag m3-cross-cutting
```

---

## Self-Review

### Spec coverage check

| Spec requirement | Task |
|---|---|
| `permission_grants` table | Task 2 |
| `PermissionDefinitionContributor` | Task 6 |
| `@PreAuthorize` works | Task 16 (UsersController) |
| Permission claims in JWT | Task 8 |
| `settings` table + Caffeine | Task 2, 9 |
| `feature_values` table + Caffeine | Task 2, 10 |
| `audit_logs` table + AOP | Task 2, 11 |
| `event_publication` table | Task 2 |
| `shedlock` table + config | Task 2, 13 |
| `blobs` table | Task 2, 14 |
| Spring MessageSource ja/en | Task 12 |
| `createdBy`/`lastModifiedBy` JPA | Tasks 3, 4 |
| Seed permissions for admin | Task 15 |

### No placeholder scan: ✓ All code is complete.

### Type consistency check
- `PermissionGrantRepository` used in `PermissionManager` — consistent.
- `SettingProvider` interface in `contracts.settings`, implemented by `DatabaseSettingProvider` — consistent.
- `FeatureChecker` interface in `contracts.features`, implemented by `DatabaseFeatureChecker` — consistent.
- `CurrentUserIdHolder.UserIdProvider` in domain-shared, implemented by `SecurityContextUserIdProvider` — consistent.
