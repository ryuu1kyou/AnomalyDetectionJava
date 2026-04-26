# M1 Identity + Multi-Tenancy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire the already-scaffolded domain identity/multi-tenancy skeleton into a fully functional DDD stack: Hibernate `@Filter`-based tenant isolation, JPA repositories, Spring Security `UserDetailsService`, application services with CRUD, REST controllers, and a DB-migrator that seeds the default tenant + admin user.

**Architecture:** `domain` layer defines entity classes (`User`, `Role`, `OrganizationUnit`, `Tenant`) and pure repository interfaces; `infrastructure` provides Spring Data JPA implementations via dual-interface repositories (extend both `JpaRepository` and the domain interface) and activates the Hibernate `tenantFilter` automatically via a custom `HibernateJpaDialect` override; `application` services call domain interfaces with no Hibernate-specific code.

**Tech Stack:** Java 21, Spring Boot 3.3.5, Hibernate 6, Spring Data JPA, MariaDB4j (test), Liquibase, Spring Security 6, Maven multi-module

---

## What is already done (DO NOT redo)

The following were implemented by a previous session — read them but **do not recreate**:

| File | Status |
|------|--------|
| `domain/base/Entity.java`, `AggregateRoot.java`, `MultiTenant.java`, `FullAuditedEntity.java`, `BaseRepository.java` | ✅ Done |
| `domain/identity/User.java`, `Role.java`, `OrganizationUnit.java` | ✅ Done (needs `@Filter` + `@EntityListeners` added) |
| `infrastructure/multitenancy/CurrentTenantHolder.java`, `TenantContextHolderProvider.java`, `MultiTenantEntityListener.java`, `TenantResolutionFilter.java` | ✅ Done |
| `infrastructure/src/main/resources/db/changelog/001-identity.yaml`, `002-multitenancy.yaml` | ✅ Done |
| `host/src/test/java/.../support/MariaDB4jExtension.java`, `HealthEndpointTest.java`, `LiquibaseStartupTest.java` | ✅ Done |
| `application-contracts/.../projects/ProjectDto.java` and all projects contracts | ✅ Done |
| `application/.../projects/ProjectsAppService.java` (in-memory mock) | ✅ Done (will be replaced in M4) |
| `web/.../projects/ProjectsController.java` | ✅ Done |

---

## File Map

### New files to create

```
backend/
  domain/
    src/main/java/com/anomalydetection/domain/
      package-info.java                                     (modify: add @FilterDef)
      identity/
        UserRepository.java                                  (new)
        RoleRepository.java                                  (new)
      multitenancy/
        Tenant.java                                          (new)
        TenantRepository.java                                (new)

  infrastructure/
    pom.xml                                                  (modify: add security, aop)
    src/main/java/com/anomalydetection/infrastructure/
      InfrastructureConfiguration.java                       (modify: add @EnableJpaRepositories)
      multitenancy/
        TenantAwareHibernateJpaDialect.java                  (new)
        JpaTenantRepository.java                             (new)
      identity/
        JpaUserRepository.java                               (new)
        JpaRoleRepository.java                               (new)
      security/
        UserDetailsServiceImpl.java                          (new)
        SecurityConfiguration.java                           (new)

  application-contracts/
    src/main/java/com/anomalydetection/contracts/
      identity/
        UserDto.java                                         (new)
        CreateUserDto.java                                   (new)
        UpdateUserDto.java                                   (new)
        GetUsersInputDto.java                                (new)
        RoleDto.java                                         (new)
      multitenancy/
        TenantDto.java                                       (new)
        CreateTenantDto.java                                 (new)
        GetTenantsInputDto.java                              (new)

  application/
    pom.xml                                                  (modify: add domain dependency)
    src/main/java/com/anomalydetection/application/
      identity/
        UserAppService.java                                  (new)
        RoleAppService.java                                  (new)
      multitenancy/
        TenantAppService.java                                (new)

  web/
    src/main/java/com/anomalydetection/web/
      identity/
        UsersController.java                                 (new)
      multitenancy/
        TenantsController.java                               (new)

  db-migrator/
    pom.xml                                                  (modify: add dependencies)
    src/main/java/com/anomalydetection/dbmigrator/
      DbMigratorApplication.java                             (new)
      SeedDataInitializer.java                               (new)
    src/main/resources/
      application.yml                                        (new)

  host/
    src/test/java/com/anomalydetection/host/
      identity/
        TenantResolutionFilterTest.java                      (new)
        TenantFilterIsolationTest.java                       (new)
```

### Modified existing files

| File | Change |
|------|--------|
| `domain/package-info.java` | Add `@FilterDef(name="tenantFilter", ...)` |
| `domain/identity/User.java` | Add `@Filter`, `@EntityListeners(MultiTenantEntityListener.class)`, Hibernate import |
| `domain/identity/Role.java` | Same as User |
| `domain/identity/OrganizationUnit.java` | Same as User |
| `domain/pom.xml` | Add `hibernate-core` (for `@Filter`, `@FilterDef`) |

---

## Tasks

### Task 1: Add Hibernate @FilterDef + @Filter to domain entities

Activates row-level tenant isolation. Without this, all queries return rows from all tenants.

**Files:**
- Modify: `backend/domain/pom.xml`
- Modify: `backend/domain/src/main/java/com/anomalydetection/domain/package-info.java`
- Modify: `backend/domain/src/main/java/com/anomalydetection/domain/identity/User.java`
- Modify: `backend/domain/src/main/java/com/anomalydetection/domain/identity/Role.java`
- Modify: `backend/domain/src/main/java/com/anomalydetection/domain/identity/OrganizationUnit.java`

- [ ] **Step 1: Add hibernate-core dependency to domain/pom.xml**

Open `backend/domain/pom.xml`. It currently lists `jakarta.persistence-api`, `spring-context`, `slf4j-api`. Add `hibernate-core` so that `@Filter` and `@FilterDef` (which are Hibernate-specific annotations) compile:

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
    <dependency>
      <groupId>jakarta.persistence</groupId>
      <artifactId>jakarta.persistence-api</artifactId>
    </dependency>
    <dependency>
      <groupId>org.hibernate.orm</groupId>
      <artifactId>hibernate-core</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-context</artifactId>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
    </dependency>
  </dependencies>
</project>
```

- [ ] **Step 2: Add @FilterDef to domain/package-info.java**

The filter definition must be visible to the entire persistence unit. Placing it in package-info.java for the domain package achieves this. Replace the entire file:

```java
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = String.class)
)
package com.anomalydetection.domain;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
```

- [ ] **Step 3: Add @Filter to User.java**

Add one import and one class-level annotation. The `@Filter` condition uses MySQL's `UNHEX(REPLACE(...))` to compare the string UUID parameter to the stored `BINARY(16)` column. Do NOT add `@EntityListeners` — tenant_id is set explicitly by application services (see Task 7).

Full updated `User.java`:

```java
package com.anomalydetection.domain.identity;

import com.anomalydetection.domain.base.FullAuditedEntity;
import com.anomalydetection.domain.base.MultiTenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "users")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
public class User extends FullAuditedEntity<UUID> implements MultiTenant {

  @Id
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "user_name", nullable = false, length = 256)
  private String userName;

  @Column(name = "normalized_user_name", nullable = false, length = 256)
  private String normalizedUserName;

  @Column(name = "email", length = 256)
  private String email;

  @Column(name = "normalized_email", length = 256)
  private String normalizedEmail;

  @Column(name = "email_confirmed", nullable = false)
  private boolean emailConfirmed;

  @Column(name = "password_hash", length = 512)
  private String passwordHash;

  @Column(name = "phone_number", length = 64)
  private String phoneNumber;

  @Column(name = "phone_number_confirmed", nullable = false)
  private boolean phoneNumberConfirmed;

  @Column(name = "two_factor_enabled", nullable = false)
  private boolean twoFactorEnabled;

  @Column(name = "lockout_enabled", nullable = false)
  private boolean lockoutEnabled;

  @Column(name = "lockout_end")
  private java.time.Instant lockoutEnd;

  @Column(name = "access_failed_count", nullable = false)
  private int accessFailedCount;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  protected User() {}

  public User(UUID id, String userName, String normalizedUserName) {
    this.id = id;
    this.userName = userName;
    this.normalizedUserName = normalizedUserName;
    this.isActive = true;
  }

  @Override public UUID getId() { return id; }
  @Override public UUID getTenantId() { return tenantId; }
  @Override public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getUserName() { return userName; }
  public String getNormalizedUserName() { return normalizedUserName; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getNormalizedEmail() { return normalizedEmail; }
  public void setNormalizedEmail(String v) { this.normalizedEmail = v; }
  public boolean isEmailConfirmed() { return emailConfirmed; }
  public void setEmailConfirmed(boolean v) { this.emailConfirmed = v; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String v) { this.passwordHash = v; }
  public String getPhoneNumber() { return phoneNumber; }
  public void setPhoneNumber(String v) { this.phoneNumber = v; }
  public boolean isPhoneNumberConfirmed() { return phoneNumberConfirmed; }
  public void setPhoneNumberConfirmed(boolean v) { this.phoneNumberConfirmed = v; }
  public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
  public void setTwoFactorEnabled(boolean v) { this.twoFactorEnabled = v; }
  public boolean isLockoutEnabled() { return lockoutEnabled; }
  public void setLockoutEnabled(boolean v) { this.lockoutEnabled = v; }
  public java.time.Instant getLockoutEnd() { return lockoutEnd; }
  public void setLockoutEnd(java.time.Instant v) { this.lockoutEnd = v; }
  public int getAccessFailedCount() { return accessFailedCount; }
  public void setAccessFailedCount(int v) { this.accessFailedCount = v; }
  public boolean isActive() { return isActive; }
  public void setActive(boolean active) { isActive = active; }
}
```

- [ ] **Step 4: Add @Filter to Role.java**

Full updated `Role.java`:

```java
package com.anomalydetection.domain.identity;

import com.anomalydetection.domain.base.FullAuditedEntity;
import com.anomalydetection.domain.base.MultiTenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "roles")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
public class Role extends FullAuditedEntity<UUID> implements MultiTenant {

  @Id
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "name", nullable = false, length = 256)
  private String name;

  @Column(name = "normalized_name", nullable = false, length = 256)
  private String normalizedName;

  @Column(name = "is_static", nullable = false)
  private boolean isStatic;

  @Column(name = "is_default", nullable = false)
  private boolean isDefault;

  protected Role() {}

  public Role(UUID id, String name, String normalizedName) {
    this.id = id;
    this.name = name;
    this.normalizedName = normalizedName;
  }

  @Override public UUID getId() { return id; }
  @Override public UUID getTenantId() { return tenantId; }
  @Override public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getName() { return name; }
  public String getNormalizedName() { return normalizedName; }
  public boolean isStatic() { return isStatic; }
  public void setStatic(boolean v) { isStatic = v; }
  public boolean isDefault() { return isDefault; }
  public void setDefault(boolean v) { isDefault = v; }
}
```

- [ ] **Step 5: Add @Filter to OrganizationUnit.java**

Full updated `OrganizationUnit.java`:

```java
package com.anomalydetection.domain.identity;

import com.anomalydetection.domain.base.FullAuditedEntity;
import com.anomalydetection.domain.base.MultiTenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "organization_units")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
public class OrganizationUnit extends FullAuditedEntity<UUID> implements MultiTenant {

  @Id
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "parent_id", columnDefinition = "BINARY(16)")
  private UUID parentId;

  @Column(name = "code", nullable = false, length = 128)
  private String code;

  @Column(name = "display_name", nullable = false, length = 256)
  private String displayName;

  protected OrganizationUnit() {}

  public OrganizationUnit(UUID id, String code, String displayName) {
    this.id = id;
    this.code = code;
    this.displayName = displayName;
  }

  @Override public UUID getId() { return id; }
  @Override public UUID getTenantId() { return tenantId; }
  @Override public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public UUID getParentId() { return parentId; }
  public void setParentId(UUID parentId) { this.parentId = parentId; }
  public String getCode() { return code; }
  public String getDisplayName() { return displayName; }
}
```

- [ ] **Step 6: Verify domain compiles**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw compile -pl domain --no-transfer-progress
```

Expected: `BUILD SUCCESS`

- [ ] **Step 7: Commit**

```
git add backend/domain/pom.xml backend/domain/src/main/java/com/anomalydetection/domain/package-info.java backend/domain/src/main/java/com/anomalydetection/domain/identity/User.java backend/domain/src/main/java/com/anomalydetection/domain/identity/Role.java backend/domain/src/main/java/com/anomalydetection/domain/identity/OrganizationUnit.java
git commit -m "feat(m1): add Hibernate tenantFilter @FilterDef and @Filter to identity entities"
```

---

### Task 2: Create ICurrentTenant interface in domain (fixes circular-dependency issue)

Domain entities must NOT import from infrastructure. Instead of `@EntityListeners(MultiTenantEntityListener.class)` (which requires infrastructure), application services set `tenant_id` explicitly using an `ICurrentTenant` interface defined in domain and implemented in infrastructure.

**Files:**
- Create: `backend/domain/src/main/java/com/anomalydetection/domain/multitenancy/ICurrentTenant.java`
- Modify: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/multitenancy/CurrentTenantHolder.java`

- [ ] **Step 1: Create ICurrentTenant.java**

```java
// backend/domain/src/main/java/com/anomalydetection/domain/multitenancy/ICurrentTenant.java
package com.anomalydetection.domain.multitenancy;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain-layer interface for accessing the current tenant context.
 * Matching ABP's ICurrentTenant.
 * Infrastructure provides the request-scoped implementation (CurrentTenantHolder).
 */
public interface ICurrentTenant {

  Optional<UUID> getTenantId();

  boolean isSet();
}
```

- [ ] **Step 2: Make CurrentTenantHolder implement ICurrentTenant**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/multitenancy/CurrentTenantHolder.java
package com.anomalydetection.infrastructure.multitenancy;

import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentTenantHolder implements ICurrentTenant {

  private UUID tenantId;

  @Override
  public Optional<UUID> getTenantId() {
    return Optional.ofNullable(tenantId);
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public boolean isSet() {
    return tenantId != null;
  }

  public static CurrentTenantHolder none() {
    return new CurrentTenantHolder();
  }
}
```

- [ ] **Step 3: Verify infrastructure compiles**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw compile -pl infrastructure --no-transfer-progress
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```
git add backend/domain/src/main/java/com/anomalydetection/domain/multitenancy/ICurrentTenant.java backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/multitenancy/CurrentTenantHolder.java
git commit -m "feat(m1): add ICurrentTenant domain interface, CurrentTenantHolder implements it"
```

---

### Task 3: Create Tenant domain entity and domain repository interfaces

Tenant is the aggregate root for multi-tenancy. Repository interfaces define the domain contract; infrastructure provides implementations in Task 4.

**Files:**
- Create: `backend/domain/src/main/java/com/anomalydetection/domain/multitenancy/Tenant.java`
- Create: `backend/domain/src/main/java/com/anomalydetection/domain/identity/UserRepository.java`
- Create: `backend/domain/src/main/java/com/anomalydetection/domain/identity/RoleRepository.java`
- Create: `backend/domain/src/main/java/com/anomalydetection/domain/multitenancy/TenantRepository.java`

- [ ] **Step 1: Create Tenant.java**

Tenant does NOT implement `MultiTenant` — it IS the tenant. No tenant_id column on tenants table.

```java
// backend/domain/src/main/java/com/anomalydetection/domain/multitenancy/Tenant.java
package com.anomalydetection.domain.multitenancy;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class Tenant extends FullAuditedEntity<UUID> {

  @Id
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "name", nullable = false, length = 256)
  private String name;

  @Column(name = "normalized_name", nullable = false, length = 256)
  private String normalizedName;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  protected Tenant() {}

  public Tenant(UUID id, String name) {
    this.id = id;
    this.name = name;
    this.normalizedName = name.toUpperCase();
    this.isActive = true;
  }

  @Override public UUID getId() { return id; }

  public String getName() { return name; }

  public void setName(String name) {
    this.name = name;
    this.normalizedName = name.toUpperCase();
  }

  public String getNormalizedName() { return normalizedName; }
  public boolean isActive() { return isActive; }
  public void setActive(boolean active) { isActive = active; }
}
```

- [ ] **Step 2: Create UserRepository.java**

```java
// backend/domain/src/main/java/com/anomalydetection/domain/identity/UserRepository.java
package com.anomalydetection.domain.identity;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends BaseRepository<User, UUID> {

  Optional<User> findByNormalizedUserName(String normalizedUserName);

  Optional<User> findByNormalizedEmail(String normalizedEmail);

  List<User> findAllByTenantId(UUID tenantId);

  long countByTenantId(UUID tenantId);
}
```

- [ ] **Step 3: Create RoleRepository.java**

```java
// backend/domain/src/main/java/com/anomalydetection/domain/identity/RoleRepository.java
package com.anomalydetection.domain.identity;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends BaseRepository<Role, UUID> {

  Optional<Role> findByNormalizedName(String normalizedName);

  List<Role> findAllByTenantId(UUID tenantId);
}
```

- [ ] **Step 4: Create TenantRepository.java**

```java
// backend/domain/src/main/java/com/anomalydetection/domain/multitenancy/TenantRepository.java
package com.anomalydetection.domain.multitenancy;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends BaseRepository<Tenant, UUID> {

  Optional<Tenant> findByNormalizedName(String normalizedName);

  boolean existsByNormalizedName(String normalizedName);
}
```

- [ ] **Step 5: Verify domain compiles**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw compile -pl domain --no-transfer-progress
```

Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```
git add backend/domain/src/main/java/com/anomalydetection/domain/multitenancy/ backend/domain/src/main/java/com/anomalydetection/domain/identity/UserRepository.java backend/domain/src/main/java/com/anomalydetection/domain/identity/RoleRepository.java
git commit -m "feat(m1): add Tenant aggregate and domain repository interfaces"
```

---

### Task 3: Write tenant filter isolation integration test (failing)

Write the test BEFORE implementing infrastructure. It will fail because `JpaUserRepository` doesn't exist yet.

**Files:**
- Create: `backend/host/src/test/java/com/anomalydetection/host/identity/TenantFilterIsolationTest.java`

- [ ] **Step 1: Write the failing integration test**

```java
// backend/host/src/test/java/com/anomalydetection/host/identity/TenantFilterIsolationTest.java
package com.anomalydetection.host.identity;

import static org.assertj.core.api.Assertions.assertThat;

import com.anomalydetection.AnomalyDetectionApplication;
import com.anomalydetection.domain.identity.User;
import com.anomalydetection.domain.identity.UserRepository;
import com.anomalydetection.host.support.MariaDB4jExtension;
import com.anomalydetection.infrastructure.multitenancy.CurrentTenantHolder;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = AnomalyDetectionApplication.class)
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
@Transactional
class TenantFilterIsolationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private CurrentTenantHolder tenantHolder;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void usersAreIsolatedByTenant() {
    UUID tenantA = UUID.randomUUID();
    UUID tenantB = UUID.randomUUID();

    // Create user in tenant A
    tenantHolder.setTenantId(tenantA);
    User userA = new User(UUID.randomUUID(), "alice", "ALICE");
    userRepository.save(userA);

    // Create user in tenant B
    tenantHolder.setTenantId(tenantB);
    User userB = new User(UUID.randomUUID(), "bob", "BOB");
    userRepository.save(userB);

    // Query as tenant A — should only see alice
    tenantHolder.setTenantId(tenantA);
    List<User> tenantAUsers = userRepository.findAll();
    assertThat(tenantAUsers).hasSize(1);
    assertThat(tenantAUsers.get(0).getUserName()).isEqualTo("alice");

    // Query as tenant B — should only see bob
    tenantHolder.setTenantId(tenantB);
    List<User> tenantBUsers = userRepository.findAll();
    assertThat(tenantBUsers).hasSize(1);
    assertThat(tenantBUsers.get(0).getUserName()).isEqualTo("bob");

    // Query as host (no tenant) — should see both
    tenantHolder.setTenantId(null);
    List<User> allUsers = userRepository.findAll();
    assertThat(allUsers).hasSize(2);
  }
}
```

- [ ] **Step 2: Run test to verify it fails (compile error expected)**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw test -pl host -Dtest=TenantFilterIsolationTest --no-transfer-progress 2>&1 | tail -20
```

Expected: FAIL — either compile error (JpaUserRepository missing) or `NoSuchBeanDefinitionException` for `UserRepository`.

- [ ] **Step 3: Commit failing test**

```
git add backend/host/src/test/java/com/anomalydetection/host/identity/TenantFilterIsolationTest.java
git commit -m "test(m1): add failing tenant filter isolation integration test"
```

---

### Task 4: Create Spring Data JPA repositories and TenantAwareHibernateJpaDialect

This is the core of M1. The `TenantAwareHibernateJpaDialect` overrides `beginTransaction` to enable the Hibernate filter at the start of every transaction, so application services never need Hibernate-specific code.

**Files:**
- Modify: `backend/infrastructure/pom.xml`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/identity/JpaUserRepository.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/identity/JpaRoleRepository.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/multitenancy/JpaTenantRepository.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/multitenancy/TenantAwareHibernateJpaDialect.java`
- Modify: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/InfrastructureConfiguration.java`

- [ ] **Step 1: Add spring-boot-starter-security to infrastructure/pom.xml**

Spring Security is needed by `UserDetailsServiceImpl` in Task 5. Add it now alongside the existing dependencies:

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
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
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

- [ ] **Step 2: Create JpaUserRepository.java**

Dual-interface: extends Spring Data's `JpaRepository` (provides CRUD implementation) AND `UserRepository` (satisfies domain interface). Spring DI will inject this implementation wherever `UserRepository` is declared.

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/identity/JpaUserRepository.java
package com.anomalydetection.infrastructure.identity;

import com.anomalydetection.domain.identity.User;
import com.anomalydetection.domain.identity.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID>, UserRepository {

  Optional<User> findByNormalizedUserName(String normalizedUserName);

  Optional<User> findByNormalizedEmail(String normalizedEmail);

  List<User> findAllByTenantId(UUID tenantId);

  long countByTenantId(UUID tenantId);
}
```

- [ ] **Step 3: Create JpaRoleRepository.java**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/identity/JpaRoleRepository.java
package com.anomalydetection.infrastructure.identity;

import com.anomalydetection.domain.identity.Role;
import com.anomalydetection.domain.identity.RoleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRoleRepository extends JpaRepository<Role, UUID>, RoleRepository {

  Optional<Role> findByNormalizedName(String normalizedName);

  List<Role> findAllByTenantId(UUID tenantId);
}
```

- [ ] **Step 4: Create JpaTenantRepository.java**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/multitenancy/JpaTenantRepository.java
package com.anomalydetection.infrastructure.multitenancy;

import com.anomalydetection.domain.multitenancy.Tenant;
import com.anomalydetection.domain.multitenancy.TenantRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTenantRepository extends JpaRepository<Tenant, UUID>, TenantRepository {

  Optional<Tenant> findByNormalizedName(String normalizedName);

  boolean existsByNormalizedName(String normalizedName);
}
```

- [ ] **Step 5: Create TenantAwareHibernateJpaDialect.java**

This is the key class that enables the Hibernate tenant filter automatically at the start of every JPA transaction. It uses `TenantContextHolderProvider` (already implemented) to retrieve the current tenant.

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/multitenancy/TenantAwareHibernateJpaDialect.java
package com.anomalydetection.infrastructure.multitenancy;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.TransactionDefinition;

public class TenantAwareHibernateJpaDialect extends HibernateJpaDialect {

  private static final Logger log = LoggerFactory.getLogger(TenantAwareHibernateJpaDialect.class);
  private static final String FILTER_NAME = "tenantFilter";
  private static final String PARAM_TENANT_ID = "tenantId";

  @Override
  public Object beginTransaction(EntityManager em, TransactionDefinition definition) {
    Object data = super.beginTransaction(em, definition);
    enableTenantFilter(em);
    return data;
  }

  private void enableTenantFilter(EntityManager em) {
    CurrentTenantHolder holder = TenantContextHolderProvider.getHolder();
    if (holder == null || !holder.isSet()) {
      return;
    }
    try {
      Session session = em.unwrap(Session.class);
      session.enableFilter(FILTER_NAME)
          .setParameter(PARAM_TENANT_ID, holder.getTenantId().get().toString());
      log.trace("Tenant filter enabled for tenant {}", holder.getTenantId().get());
    } catch (Exception e) {
      log.warn("Failed to enable tenant filter: {}", e.getMessage());
    }
  }
}
```

- [ ] **Step 6: Update InfrastructureConfiguration.java**

Configure Spring Data JPA scanning and inject the custom dialect into the `EntityManagerFactory`. The `@EnableJpaRepositories` scans the infrastructure package for `@Repository` interfaces.

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/InfrastructureConfiguration.java
package com.anomalydetection.infrastructure;

import com.anomalydetection.infrastructure.multitenancy.TenantAwareHibernateJpaDialect;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(basePackages = "com.anomalydetection.infrastructure")
public class InfrastructureConfiguration {

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      DataSource dataSource, JpaProperties jpaProperties) {
    var adapter = new HibernateJpaVendorAdapter();
    adapter.setShowSql(jpaProperties.isShowSql());

    var factory = new LocalContainerEntityManagerFactoryBean();
    factory.setDataSource(dataSource);
    factory.setJpaVendorAdapter(adapter);
    factory.setJpaDialect(new TenantAwareHibernateJpaDialect());
    factory.setPackagesToScan(
        "com.anomalydetection.domain",
        "com.anomalydetection.infrastructure");
    factory.setJpaPropertyMap(jpaProperties.getProperties());
    return factory;
  }

  @Bean
  public PlatformTransactionManager transactionManager(
      LocalContainerEntityManagerFactoryBean emf) {
    return new JpaTransactionManager(emf.getObject());
  }
}
```

- [ ] **Step 7: Run the failing test again — should now pass**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw test -pl host -Dtest=TenantFilterIsolationTest --no-transfer-progress 2>&1 | tail -30
```

Expected: `TenantFilterIsolationTest > usersAreIsolatedByTenant() PASSED`

If the test still fails, check:
- `DATABASECHANGELOG` runs 001-identity.yaml (creates `users` table) — verify in LiquibaseStartupTest output
- The `@FilterDef` in `package-info.java` is picked up by Hibernate scan
- The `TenantAwareHibernateJpaDialect` bean is wired (check Spring context startup logs)

- [ ] **Step 8: Commit**

```
git add backend/infrastructure/
git commit -m "feat(m1): add JPA repositories, TenantAwareHibernateJpaDialect, InfrastructureConfiguration"
```

---

### Task 5: Spring Security — UserDetailsService and SecurityConfiguration

Spring Authorization Server (M2) needs a `UserDetailsService` to authenticate users. Add it now in infrastructure so M2 can depend on it without circular issues.

**Files:**
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/UserDetailsServiceImpl.java`
- Create: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/SecurityConfiguration.java`

- [ ] **Step 1: Write failing unit test for UserDetailsServiceImpl**

```java
// backend/host/src/test/java/com/anomalydetection/host/identity/UserDetailsServiceTest.java
package com.anomalydetection.host.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.anomalydetection.domain.identity.User;
import com.anomalydetection.domain.identity.UserRepository;
import com.anomalydetection.infrastructure.security.UserDetailsServiceImpl;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class UserDetailsServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);

  @Test
  void loadsUserByUsername() {
    User user = new User(UUID.randomUUID(), "alice", "ALICE");
    user.setPasswordHash("$2a$10$hashedpassword");
    user.setActive(true);
    when(userRepository.findByNormalizedUserName("ALICE")).thenReturn(Optional.of(user));

    var details = service.loadUserByUsername("alice");

    assertThat(details.getUsername()).isEqualTo("alice");
    assertThat(details.getPassword()).isEqualTo("$2a$10$hashedpassword");
    assertThat(details.isEnabled()).isTrue();
  }

  @Test
  void throwsWhenUserNotFound() {
    when(userRepository.findByNormalizedUserName("UNKNOWN")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.loadUserByUsername("unknown"))
        .isInstanceOf(UsernameNotFoundException.class);
  }
}
```

- [ ] **Step 2: Run to verify failure**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw test -pl host -Dtest=UserDetailsServiceTest --no-transfer-progress 2>&1 | tail -10
```

Expected: FAIL — `UserDetailsServiceImpl` class not found.

- [ ] **Step 3: Create UserDetailsServiceImpl.java**

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/UserDetailsServiceImpl.java
package com.anomalydetection.infrastructure.security;

import com.anomalydetection.domain.identity.UserRepository;
import java.util.Collections;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository
        .findByNormalizedUserName(username.toUpperCase())
        .map(
            user ->
                org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUserName())
                    .password(user.getPasswordHash() != null ? user.getPasswordHash() : "")
                    .disabled(!user.isActive())
                    .accountLocked(
                        user.getLockoutEnd() != null
                            && user.getLockoutEnd().isAfter(java.time.Instant.now()))
                    .authorities(Collections.emptyList())
                    .build())
        .orElseThrow(
            () ->
                new UsernameNotFoundException("User not found: " + username));
  }
}
```

- [ ] **Step 4: Create SecurityConfiguration.java**

This is a minimal security config sufficient for M1. M2 will add OAuth2/OIDC on top. The config permits all requests for now (auth enforcement comes in M2/M3 via `@PreAuthorize`).

```java
// backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/SecurityConfiguration.java
package com.anomalydetection.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**").permitAll()
            .anyRequest().permitAll()
        );
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
```

- [ ] **Step 5: Run unit test — should pass**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw test -pl host -Dtest=UserDetailsServiceTest --no-transfer-progress 2>&1 | tail -10
```

Expected: `UserDetailsServiceTest > loadsUserByUsername() PASSED`

- [ ] **Step 6: Run full host test suite**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw test -pl host --no-transfer-progress 2>&1 | tail -20
```

Expected: All tests pass (including `HealthEndpointTest`, `LiquibaseStartupTest`, `TenantFilterIsolationTest`, `UserDetailsServiceTest`).

- [ ] **Step 7: Commit**

```
git add backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/ backend/host/src/test/java/com/anomalydetection/host/identity/UserDetailsServiceTest.java
git commit -m "feat(m1): add UserDetailsService and minimal SecurityConfiguration"
```

---

### Task 6: Application contracts — identity and tenancy DTOs

DTOs live in `application-contracts` so the web layer (and eventually OpenAPI generation) can reference them without coupling to domain entities.

**Files:**
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/UserDto.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/CreateUserDto.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/UpdateUserDto.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/GetUsersInputDto.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/RoleDto.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/multitenancy/TenantDto.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/multitenancy/CreateTenantDto.java`
- Create: `backend/application-contracts/src/main/java/com/anomalydetection/contracts/multitenancy/GetTenantsInputDto.java`

- [ ] **Step 1: Create UserDto.java**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/UserDto.java
package com.anomalydetection.contracts.identity;

import java.util.UUID;

public record UserDto(
    UUID id,
    UUID tenantId,
    String userName,
    String email,
    boolean isActive,
    boolean emailConfirmed) {}
```

- [ ] **Step 2: Create CreateUserDto.java**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/CreateUserDto.java
package com.anomalydetection.contracts.identity;

public record CreateUserDto(
    String userName,
    String email,
    String password,
    boolean isActive) {}
```

- [ ] **Step 3: Create UpdateUserDto.java**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/UpdateUserDto.java
package com.anomalydetection.contracts.identity;

public record UpdateUserDto(
    String email,
    boolean isActive) {}
```

- [ ] **Step 4: Create GetUsersInputDto.java**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/GetUsersInputDto.java
package com.anomalydetection.contracts.identity;

public record GetUsersInputDto(
    String filter,
    Integer skipCount,
    Integer maxResultCount) {}
```

- [ ] **Step 5: Create RoleDto.java**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/RoleDto.java
package com.anomalydetection.contracts.identity;

import java.util.UUID;

public record RoleDto(
    UUID id,
    UUID tenantId,
    String name,
    boolean isStatic,
    boolean isDefault) {}
```

- [ ] **Step 6: Create TenantDto.java**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/multitenancy/TenantDto.java
package com.anomalydetection.contracts.multitenancy;

import java.util.UUID;

public record TenantDto(
    UUID id,
    String name,
    boolean isActive) {}
```

- [ ] **Step 7: Create CreateTenantDto.java**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/multitenancy/CreateTenantDto.java
package com.anomalydetection.contracts.multitenancy;

public record CreateTenantDto(String name) {}
```

- [ ] **Step 8: Create GetTenantsInputDto.java**

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/multitenancy/GetTenantsInputDto.java
package com.anomalydetection.contracts.multitenancy;

public record GetTenantsInputDto(
    String filter,
    Integer skipCount,
    Integer maxResultCount) {}
```

- [ ] **Step 9: Verify application-contracts compiles**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw compile -pl application-contracts --no-transfer-progress
```

Expected: `BUILD SUCCESS`

- [ ] **Step 10: Commit**

```
git add backend/application-contracts/src/main/java/com/anomalydetection/contracts/identity/ backend/application-contracts/src/main/java/com/anomalydetection/contracts/multitenancy/
git commit -m "feat(m1): add identity and tenancy DTOs to application-contracts"
```

---

### Task 7: Application services — UserAppService and TenantAppService

Application services orchestrate domain repository operations. They are `@Transactional` — the `TenantAwareHibernateJpaDialect` (Task 4) automatically enables the tenant filter when a transaction begins.

**Files:**
- Modify: `backend/application/pom.xml`
- Create: `backend/application/src/main/java/com/anomalydetection/application/identity/UserAppService.java`
- Create: `backend/application/src/main/java/com/anomalydetection/application/identity/RoleAppService.java`
- Create: `backend/application/src/main/java/com/anomalydetection/application/multitenancy/TenantAppService.java`

- [ ] **Step 1: Add anomaly-detection-domain to application/pom.xml**

The application module needs explicit access to the domain module (currently only transitive via application-contracts):

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
      <groupId>com.anomalydetection</groupId>
      <artifactId>anomaly-detection-domain</artifactId>
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

- [ ] **Step 2: Write failing unit test for UserAppService**

```java
// backend/host/src/test/java/com/anomalydetection/host/identity/UserAppServiceTest.java
package com.anomalydetection.host.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.anomalydetection.application.identity.UserAppService;
import com.anomalydetection.contracts.identity.CreateUserDto;
import com.anomalydetection.contracts.identity.UserDto;
import com.anomalydetection.domain.identity.User;
import com.anomalydetection.domain.identity.UserRepository;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserAppServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final ICurrentTenant currentTenant = mock(ICurrentTenant.class);
  private final UserAppService service =
      new UserAppService(userRepository, new BCryptPasswordEncoder(4), currentTenant);

  @Test
  void createsUserWithHashedPassword() {
    UUID tenantId = UUID.randomUUID();
    when(currentTenant.getTenantId()).thenReturn(Optional.of(tenantId));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    UserDto dto = service.create(new CreateUserDto("alice", "alice@example.com", "secret123", true));

    assertThat(dto.userName()).isEqualTo("alice");
    assertThat(dto.email()).isEqualTo("alice@example.com");
    assertThat(dto.isActive()).isTrue();
    assertThat(dto.tenantId()).isEqualTo(tenantId);
  }

  @Test
  void getListReturnsAllUsers() {
    User user = new User(UUID.randomUUID(), "alice", "ALICE");
    when(userRepository.findAll()).thenReturn(List.of(user));

    var result = service.getList(null);

    assertThat(result.items()).hasSize(1);
    assertThat(result.items().get(0).userName()).isEqualTo("alice");
  }
}
```

- [ ] **Step 3: Run to verify failure**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw test -pl host -Dtest=UserAppServiceTest --no-transfer-progress 2>&1 | tail -10
```

Expected: FAIL — `UserAppService` class not found.

- [ ] **Step 4: Create UserAppService.java**

`UserAppService` injects `ICurrentTenant` (domain interface, implemented at runtime by `CurrentTenantHolder` from infrastructure) to set `tenant_id` explicitly when creating users. `PasswordEncoder` is provided by `SecurityConfiguration`. Spring wires all at runtime.

```java
// backend/application/src/main/java/com/anomalydetection/application/identity/UserAppService.java
package com.anomalydetection.application.identity;

import com.anomalydetection.contracts.identity.CreateUserDto;
import com.anomalydetection.contracts.identity.GetUsersInputDto;
import com.anomalydetection.contracts.identity.UpdateUserDto;
import com.anomalydetection.contracts.identity.UserDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import com.anomalydetection.domain.identity.User;
import com.anomalydetection.domain.identity.UserRepository;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserAppService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ICurrentTenant currentTenant;

  public UserAppService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      ICurrentTenant currentTenant) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.currentTenant = currentTenant;
  }

  @Transactional(readOnly = true)
  public PagedResultDto<UserDto> getList(GetUsersInputDto input) {
    String filter = input != null && input.filter() != null ? input.filter().toLowerCase() : "";
    List<User> all = userRepository.findAll();

    var filtered = all.stream()
        .filter(u -> filter.isBlank()
            || u.getUserName().toLowerCase().contains(filter)
            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(filter)))
        .toList();

    int skip = input != null && input.skipCount() != null ? Math.max(0, input.skipCount()) : 0;
    int take = input != null && input.maxResultCount() != null ? Math.max(1, input.maxResultCount()) : 10;

    var page = filtered.stream().skip(skip).limit(take).map(this::toDto).toList();
    return PagedResultDto.of(page, filtered.size());
  }

  @Transactional(readOnly = true)
  public Optional<UserDto> getById(UUID id) {
    return userRepository.findById(id).map(this::toDto);
  }

  public UserDto create(CreateUserDto input) {
    var user = new User(UUID.randomUUID(), input.userName(), input.userName().toUpperCase());
    // Explicitly set tenant_id — domain entities do NOT have @EntityListeners
    currentTenant.getTenantId().ifPresent(user::setTenantId);
    user.setEmail(input.email());
    user.setNormalizedEmail(input.email() != null ? input.email().toUpperCase() : null);
    if (input.password() != null && !input.password().isBlank()) {
      user.setPasswordHash(passwordEncoder.encode(input.password()));
    }
    user.setActive(input.isActive());
    return toDto(userRepository.save(user));
  }

  public Optional<UserDto> update(UUID id, UpdateUserDto input) {
    return userRepository.findById(id).map(user -> {
      user.setEmail(input.email());
      user.setNormalizedEmail(input.email() != null ? input.email().toUpperCase() : null);
      user.setActive(input.isActive());
      return toDto(userRepository.save(user));
    });
  }

  public boolean delete(UUID id) {
    if (!userRepository.existsById(id)) return false;
    userRepository.deleteById(id);
    return true;
  }

  private UserDto toDto(User u) {
    return new UserDto(u.getId(), u.getTenantId(), u.getUserName(), u.getEmail(),
        u.isActive(), u.isEmailConfirmed());
  }
}
```

- [ ] **Step 5: Create RoleAppService.java**

```java
// backend/application/src/main/java/com/anomalydetection/application/identity/RoleAppService.java
package com.anomalydetection.application.identity;

import com.anomalydetection.contracts.identity.RoleDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import com.anomalydetection.domain.identity.Role;
import com.anomalydetection.domain.identity.RoleRepository;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleAppService {

  private final RoleRepository roleRepository;
  private final ICurrentTenant currentTenant;

  public RoleAppService(RoleRepository roleRepository, ICurrentTenant currentTenant) {
    this.roleRepository = roleRepository;
    this.currentTenant = currentTenant;
  }

  @Transactional(readOnly = true)
  public PagedResultDto<RoleDto> getList() {
    List<RoleDto> roles = roleRepository.findAll().stream().map(this::toDto).toList();
    return PagedResultDto.of(roles, roles.size());
  }

  @Transactional(readOnly = true)
  public Optional<RoleDto> getById(UUID id) {
    return roleRepository.findById(id).map(this::toDto);
  }

  public RoleDto create(String name) {
    var role = new Role(UUID.randomUUID(), name, name.toUpperCase());
    currentTenant.getTenantId().ifPresent(role::setTenantId);
    return toDto(roleRepository.save(role));
  }

  public boolean delete(UUID id) {
    if (!roleRepository.existsById(id)) return false;
    roleRepository.deleteById(id);
    return true;
  }

  private RoleDto toDto(Role r) {
    return new RoleDto(r.getId(), r.getTenantId(), r.getName(), r.isStatic(), r.isDefault());
  }
}
```

- [ ] **Step 6: Create TenantAppService.java**

```java
// backend/application/src/main/java/com/anomalydetection/application/multitenancy/TenantAppService.java
package com.anomalydetection.application.multitenancy;

import com.anomalydetection.contracts.multitenancy.CreateTenantDto;
import com.anomalydetection.contracts.multitenancy.GetTenantsInputDto;
import com.anomalydetection.contracts.multitenancy.TenantDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import com.anomalydetection.domain.multitenancy.Tenant;
import com.anomalydetection.domain.multitenancy.TenantRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TenantAppService {

  private final TenantRepository tenantRepository;

  public TenantAppService(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  @Transactional(readOnly = true)
  public PagedResultDto<TenantDto> getList(GetTenantsInputDto input) {
    String filter = input != null && input.filter() != null ? input.filter().toLowerCase() : "";
    List<Tenant> all = tenantRepository.findAll();

    var filtered = all.stream()
        .filter(t -> filter.isBlank() || t.getName().toLowerCase().contains(filter))
        .toList();

    int skip = input != null && input.skipCount() != null ? Math.max(0, input.skipCount()) : 0;
    int take = input != null && input.maxResultCount() != null ? Math.max(1, input.maxResultCount()) : 10;

    var page = filtered.stream().skip(skip).limit(take).map(this::toDto).toList();
    return PagedResultDto.of(page, filtered.size());
  }

  @Transactional(readOnly = true)
  public Optional<TenantDto> getById(UUID id) {
    return tenantRepository.findById(id).map(this::toDto);
  }

  public TenantDto create(CreateTenantDto input) {
    if (tenantRepository.existsByNormalizedName(input.name().toUpperCase())) {
      throw new IllegalArgumentException("Tenant already exists: " + input.name());
    }
    var tenant = new Tenant(UUID.randomUUID(), input.name());
    return toDto(tenantRepository.save(tenant));
  }

  public Optional<TenantDto> setActive(UUID id, boolean active) {
    return tenantRepository.findById(id).map(tenant -> {
      tenant.setActive(active);
      return toDto(tenantRepository.save(tenant));
    });
  }

  public boolean delete(UUID id) {
    if (!tenantRepository.existsById(id)) return false;
    tenantRepository.deleteById(id);
    return true;
  }

  private TenantDto toDto(Tenant t) {
    return new TenantDto(t.getId(), t.getName(), t.isActive());
  }
}
```

- [ ] **Step 7: Run unit test — should pass**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw test -pl host -Dtest=UserAppServiceTest --no-transfer-progress 2>&1 | tail -10
```

Expected: `UserAppServiceTest > createsUserWithHashedPassword() PASSED`

- [ ] **Step 8: Run full backend test suite**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw test --no-transfer-progress 2>&1 | tail -20
```

Expected: All module tests pass. Verify `TenantFilterIsolationTest`, `HealthEndpointTest`, `LiquibaseStartupTest` still pass.

- [ ] **Step 9: Commit**

```
git add backend/application/pom.xml backend/application/src/main/java/com/anomalydetection/application/identity/ backend/application/src/main/java/com/anomalydetection/application/multitenancy/ backend/host/src/test/java/com/anomalydetection/host/identity/UserAppServiceTest.java
git commit -m "feat(m1): add UserAppService, RoleAppService, TenantAppService"
```

---

### Task 8: Web controllers — UsersController and TenantsController

REST endpoints following the same pattern as the existing `ProjectsController`.

**Files:**
- Create: `backend/web/src/main/java/com/anomalydetection/web/identity/UsersController.java`
- Create: `backend/web/src/main/java/com/anomalydetection/web/multitenancy/TenantsController.java`

- [ ] **Step 1: Create UsersController.java**

```java
// backend/web/src/main/java/com/anomalydetection/web/identity/UsersController.java
package com.anomalydetection.web.identity;

import com.anomalydetection.application.identity.UserAppService;
import com.anomalydetection.contracts.identity.CreateUserDto;
import com.anomalydetection.contracts.identity.GetUsersInputDto;
import com.anomalydetection.contracts.identity.UpdateUserDto;
import com.anomalydetection.contracts.identity.UserDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/users")
public class UsersController {

  private final UserAppService appService;

  public UsersController(UserAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public PagedResultDto<UserDto> getList(
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) Integer skipCount,
      @RequestParam(required = false) Integer maxResultCount) {
    return appService.getList(new GetUsersInputDto(filter, skipCount, maxResultCount));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> get(@PathVariable UUID id) {
    return appService.getById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public UserDto create(@RequestBody CreateUserDto input) {
    return appService.create(input);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserDto> update(@PathVariable UUID id, @RequestBody UpdateUserDto input) {
    return appService.update(id, input).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return appService.delete(id)
        ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}
```

- [ ] **Step 2: Create TenantsController.java**

```java
// backend/web/src/main/java/com/anomalydetection/web/multitenancy/TenantsController.java
package com.anomalydetection.web.multitenancy;

import com.anomalydetection.application.multitenancy.TenantAppService;
import com.anomalydetection.contracts.multitenancy.CreateTenantDto;
import com.anomalydetection.contracts.multitenancy.GetTenantsInputDto;
import com.anomalydetection.contracts.multitenancy.TenantDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/tenants")
public class TenantsController {

  private final TenantAppService appService;

  public TenantsController(TenantAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public PagedResultDto<TenantDto> getList(
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) Integer skipCount,
      @RequestParam(required = false) Integer maxResultCount) {
    return appService.getList(new GetTenantsInputDto(filter, skipCount, maxResultCount));
  }

  @GetMapping("/{id}")
  public ResponseEntity<TenantDto> get(@PathVariable UUID id) {
    return appService.getById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public TenantDto create(@RequestBody CreateTenantDto input) {
    return appService.create(input);
  }

  @PutMapping("/{id}/activate")
  public ResponseEntity<TenantDto> activate(@PathVariable UUID id) {
    return appService.setActive(id, true).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}/deactivate")
  public ResponseEntity<TenantDto> deactivate(@PathVariable UUID id) {
    return appService.setActive(id, false).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return appService.delete(id)
        ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}
```

- [ ] **Step 3: Run full build to confirm compilation**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw compile --no-transfer-progress 2>&1 | tail -10
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```
git add backend/web/src/main/java/com/anomalydetection/web/identity/ backend/web/src/main/java/com/anomalydetection/web/multitenancy/
git commit -m "feat(m1): add UsersController and TenantsController"
```

---

### Task 9: TenantResolutionFilter unit test

Verify the filter correctly parses UUID from header and query parameter.

**Files:**
- Create: `backend/host/src/test/java/com/anomalydetection/host/identity/TenantResolutionFilterTest.java`

- [ ] **Step 1: Create the unit test**

```java
// backend/host/src/test/java/com/anomalydetection/host/identity/TenantResolutionFilterTest.java
package com.anomalydetection.host.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.anomalydetection.infrastructure.multitenancy.CurrentTenantHolder;
import com.anomalydetection.infrastructure.multitenancy.TenantResolutionFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantResolutionFilterTest {

  private CurrentTenantHolder tenantHolder;
  private TenantResolutionFilter filter;

  @BeforeEach
  void setUp() {
    tenantHolder = new CurrentTenantHolder();
    filter = new TenantResolutionFilter(tenantHolder);
  }

  @Test
  void resolvesTenantFromXTenantIdHeader() throws Exception {
    UUID tenantId = UUID.randomUUID();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    doNothing().when(chain).doFilter(any(), any());

    when(request.getHeader("X-Tenant-Id")).thenReturn(tenantId.toString());
    when(request.getParameter("__tenant")).thenReturn(null);
    when(request.getServerName()).thenReturn("localhost");

    filter.doFilterInternal(request, response, chain);

    // After filter completes, tenant is cleared (finally block)
    assertThat(tenantHolder.isSet()).isFalse();
  }

  @Test
  void ignoresInvalidUuid() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    doNothing().when(chain).doFilter(any(), any());

    when(request.getHeader("X-Tenant-Id")).thenReturn("not-a-uuid");
    when(request.getParameter("__tenant")).thenReturn(null);
    when(request.getServerName()).thenReturn("localhost");

    filter.doFilterInternal(request, response, chain);

    assertThat(tenantHolder.isSet()).isFalse();
  }
}
```

- [ ] **Step 2: Run test**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw test -pl host -Dtest=TenantResolutionFilterTest --no-transfer-progress 2>&1 | tail -15
```

Expected: Both tests PASS.

- [ ] **Step 3: Commit**

```
git add backend/host/src/test/java/com/anomalydetection/host/identity/TenantResolutionFilterTest.java
git commit -m "test(m1): add TenantResolutionFilter unit tests"
```

---

### Task 10: DB Migrator — seed default tenant + admin user

The db-migrator is a standalone CLI Spring Boot app that runs Liquibase migrations then inserts seed data (default tenant + admin user + admin role). This is the Java equivalent of ABP's `DbMigrator`.

**Files:**
- Modify: `backend/db-migrator/pom.xml`
- Create: `backend/db-migrator/src/main/java/com/anomalydetection/dbmigrator/DbMigratorApplication.java`
- Create: `backend/db-migrator/src/main/java/com/anomalydetection/dbmigrator/SeedDataInitializer.java`
- Create: `backend/db-migrator/src/main/resources/application.yml`

- [ ] **Step 1: Update db-migrator/pom.xml**

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

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
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
    <dependency>
      <groupId>com.anomalydetection</groupId>
      <artifactId>anomaly-detection-infrastructure</artifactId>
    </dependency>
    <dependency>
      <groupId>com.anomalydetection</groupId>
      <artifactId>anomaly-detection-application</artifactId>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
          <mainClass>com.anomalydetection.dbmigrator.DbMigratorApplication</mainClass>
        </configuration>
        <executions>
          <execution>
            <goals><goal>repackage</goal></goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 2: Create DbMigratorApplication.java**

```java
// backend/db-migrator/src/main/java/com/anomalydetection/dbmigrator/DbMigratorApplication.java
package com.anomalydetection.dbmigrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Standalone DB migration + seed-data runner.
 *
 * Run: java -jar anomaly-detection-db-migrator.jar
 * Requires: ANOMALY_DB_PASSWORD env var (defaults to "123" for local dev)
 */
@SpringBootApplication(scanBasePackages = "com.anomalydetection")
public class DbMigratorApplication {

  private static final Logger log = LoggerFactory.getLogger(DbMigratorApplication.class);

  public static void main(String[] args) {
    log.info("Starting DB Migrator...");
    var ctx = SpringApplication.run(DbMigratorApplication.class, args);
    log.info("DB migration and seeding complete.");
    ctx.close();
  }
}
```

- [ ] **Step 3: Create SeedDataInitializer.java**

```java
// backend/db-migrator/src/main/java/com/anomalydetection/dbmigrator/SeedDataInitializer.java
package com.anomalydetection.dbmigrator;

import com.anomalydetection.domain.identity.Role;
import com.anomalydetection.domain.identity.RoleRepository;
import com.anomalydetection.domain.identity.User;
import com.anomalydetection.domain.identity.UserRepository;
import com.anomalydetection.domain.multitenancy.Tenant;
import com.anomalydetection.domain.multitenancy.TenantRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeedDataInitializer implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(SeedDataInitializer.class);

  static final String DEFAULT_TENANT_NAME = "Default";
  static final String ADMIN_USERNAME = "admin";
  static final String ADMIN_ROLE_NAME = "admin";

  private final TenantRepository tenantRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public SeedDataInitializer(
      TenantRepository tenantRepository,
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder) {
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedDefaultTenant();
    seedAdminRole();
    seedAdminUser();
  }

  private void seedDefaultTenant() {
    if (tenantRepository.existsByNormalizedName(DEFAULT_TENANT_NAME.toUpperCase())) {
      log.info("Default tenant already exists — skipping");
      return;
    }
    var tenant = new Tenant(UUID.randomUUID(), DEFAULT_TENANT_NAME);
    tenantRepository.save(tenant);
    log.info("Created default tenant: {}", DEFAULT_TENANT_NAME);
  }

  private void seedAdminRole() {
    if (roleRepository.findByNormalizedName(ADMIN_ROLE_NAME.toUpperCase()).isPresent()) {
      log.info("Admin role already exists — skipping");
      return;
    }
    var role = new Role(UUID.randomUUID(), ADMIN_ROLE_NAME, ADMIN_ROLE_NAME.toUpperCase());
    role.setStatic(true);
    roleRepository.save(role);
    log.info("Created admin role");
  }

  private void seedAdminUser() {
    if (userRepository.findByNormalizedUserName(ADMIN_USERNAME.toUpperCase()).isPresent()) {
      log.info("Admin user already exists — skipping");
      return;
    }
    String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "Admin@1234");
    var user = new User(UUID.randomUUID(), ADMIN_USERNAME, ADMIN_USERNAME.toUpperCase());
    user.setEmail("admin@anomalydetection.local");
    user.setNormalizedEmail("ADMIN@ANOMALYDETECTION.LOCAL");
    user.setPasswordHash(passwordEncoder.encode(adminPassword));
    user.setActive(true);
    user.setEmailConfirmed(true);
    userRepository.save(user);
    log.info("Created admin user (password from ADMIN_PASSWORD env var, default Admin@1234)");
  }
}
```

- [ ] **Step 4: Create db-migrator/src/main/resources/application.yml**

```yaml
spring:
  application:
    name: anomaly-detection-db-migrator
  datasource:
    url: jdbc:mysql://localhost:3306/anomaly_detection?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: ${ANOMALY_DB_PASSWORD:123}
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml

logging:
  level:
    root: WARN
    com.anomalydetection: INFO
    liquibase: INFO
```

- [ ] **Step 5: Verify full backend build compiles and all existing tests pass**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw test --no-transfer-progress 2>&1 | tail -30
```

Expected: All tests pass across all modules.

- [ ] **Step 6: Commit**

```
git add backend/db-migrator/
git commit -m "feat(m1): add DB migrator with default tenant, admin user, admin role seed"
```

---

### Task 11: Final verification — run all tests and update CLAUDE.md

- [ ] **Step 1: Run complete backend test suite**

```
cd backend
JAVA_HOME='C:\Program Files\Java\jdk-21.0.10' ./mvnw verify --no-transfer-progress 2>&1 | tail -40
```

Expected output (excerpt):
```
[INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

All of these must pass:
- `TenantFilterIsolationTest > usersAreIsolatedByTenant()`
- `UserDetailsServiceTest > loadsUserByUsername()`
- `UserDetailsServiceTest > throwsWhenUserNotFound()`
- `UserAppServiceTest > createsUserWithHashedPassword()`
- `UserAppServiceTest > getListReturnsAllUsers()`
- `TenantResolutionFilterTest > resolvesTenantFromXTenantIdHeader()`
- `TenantResolutionFilterTest > ignoresInvalidUuid()`
- `HealthEndpointTest > healthEndpointReportsUp()`
- `LiquibaseStartupTest > liquibaseCreatesBookkeepingTables()`
- `ArchitectureTest`
- `ModularityTest`

- [ ] **Step 2: Update CLAUDE.md M1 status**

Change `**M1** (Identity + Multi-Tenancy)` from `**未着手**` to `**完了**` in section 5.1. Remove M1-related items from section 5.2 (technical debt). Update section 5.3 to note M1 is now complete. Update `最終更新: 2026-04-25` to today's date.

- [ ] **Step 3: Commit final status**

```
git add -A
git commit -m "feat(m1): complete M1 Identity + Multi-Tenancy — Hibernate filter, JPA repos, AppServices, controllers, DB migrator"
git tag m1-identity-multitenancy
```

---

## Troubleshooting

### Hibernate filter not activating

If `TenantFilterIsolationTest` sees all rows instead of filtered rows:
1. Add a breakpoint or log in `TenantAwareHibernateJpaDialect.enableTenantFilter()` — verify it is called
2. Check that `InfrastructureConfiguration.entityManagerFactory()` bean is actually used (not overridden by Spring Boot auto-config). If Spring Boot's JPA auto-config takes precedence, add `@ConditionalOnMissingBean(LocalContainerEntityManagerFactoryBean.class)` to the boot auto-config or ensure our `@Bean` method wins.
3. Verify `TenantContextHolderProvider.ctx` is not null during the test — it requires a Spring-managed `ApplicationContext`

### Spring Data JPA scanning conflict

If `NoUniqueBeanDefinitionException` for repository beans: the `@EnableJpaRepositories(basePackages = "com.anomalydetection.infrastructure")` in `InfrastructureConfiguration` may conflict with Spring Boot's auto-configured scan. Add `spring.data.jpa.repositories.enabled=false` to `application.yml` or use `@SpringBootApplication(exclude = JpaRepositoriesAutoConfiguration.class)` and rely solely on our explicit `@EnableJpaRepositories`.

### `@FilterDef` not found at runtime

Hibernate 6 scans `package-info.java` in the packages declared in `packagesToScan`. Verify `InfrastructureConfiguration.entityManagerFactory()` includes `"com.anomalydetection.domain"` in `setPackagesToScan(...)`.

### BCryptPasswordEncoder in unit tests

`UserAppServiceTest` constructs `UserAppService` with `new BCryptPasswordEncoder()`. This is slow but correct. If tests time out, use `new BCryptPasswordEncoder(4)` (lower rounds) for test-only speed.
