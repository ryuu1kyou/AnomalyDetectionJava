# M2 Spring Authorization Server Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Embed a fully working OAuth2/OIDC authorization server (Spring Authorization Server 1.3.x) into the host application and protect all REST API endpoints with JWT Bearer authentication.

**Architecture:** Two `SecurityFilterChain` beans co-resident in the same monolith. `@Order(1)` handles all Spring Authorization Server protocol endpoints (`/oauth2/authorize`, `/oauth2/token`, `/oauth2/jwks`, `/userinfo`, `/.well-known/openid-configuration`). `@Order(2)` handles REST API protection via JWT Bearer and serves the form-login page required for the authorization code flow redirect. RSA 2048-bit key pair is generated at startup. Client registrations and authorization tokens are stored in MySQL (Liquibase-managed tables). The SPA client (`anomaly-detection-spa`) is seeded by the DB migrator.

**Tech Stack:** Spring Authorization Server 1.3.x (managed by Spring Boot BOM), Spring Security OAuth2 Resource Server, Nimbus JOSE+JWT (transitive via SAS), JUnit 5 + MockMvc + `spring-security-test`, MariaDB4j (tests), Liquibase

---

## What is already done (DO NOT redo)

| File | Status |
|------|--------|
| `infrastructure/security/UserDetailsServiceImpl.java` | ✅ Done — used as the user auth delegate |
| `infrastructure/security/SecurityConfiguration.java` | ✅ Done stub — **will be replaced in Task 5** |
| `auth-server/src/main/java/.../authserver/package-info.java` | ✅ Done stub — module scaffold only |
| `host/src/main/resources/application.yml` + `application-local.yml` | ✅ Done — will be updated in Task 8 |
| `infrastructure/resources/db/changelog/db.changelog-master.yaml` | ✅ Done — will be updated in Task 2 |
| All M1 identity / multitenancy domain + infrastructure | ✅ Done |

---

## File Map

### New files to create

```
backend/
  auth-server/
    src/main/java/com/anomalydetection/authserver/
      RsaKeyGenerator.java                                     (new — RSA 2048 key pair utility)
      AuthorizationServerConfig.java                           (new — @Order(1) filter chain + AS beans)

  infrastructure/
    src/main/resources/db/changelog/
      003-oauth2-authorization-server.yaml                     (new — 3 AS tables)

  host/
    src/test/java/com/anomalydetection/host/auth/
      OAuth2EndpointsTest.java                                 (new — OIDC discovery + JWKS reachable)
      ResourceServerProtectionTest.java                        (new — API returns 401/200 based on JWT)
```

### Modified files

| File | Change |
|------|--------|
| `backend/pom.xml` (parent) | No change needed — spring-security-oauth2-authorization-server is managed by Spring Boot BOM via Spring Security BOM |
| `backend/auth-server/pom.xml` | Add `spring-security-oauth2-authorization-server` dependency |
| `backend/db-migrator/pom.xml` | Add `spring-security-oauth2-authorization-server` dependency (for RegisteredClient builder in seeder) |
| `backend/host/pom.xml` | Add `spring-security-test` test dependency |
| `infrastructure/resources/db/changelog/db.changelog-master.yaml` | Include 003 changelog |
| `infrastructure/security/SecurityConfiguration.java` | Replace stub with @Order(2) resource server + form-login chain |
| `db-migrator/SeedDataInitializer.java` | Add OAuth2 SPA client seeding |
| `host/src/main/resources/application.yml` | Add issuer URI |

---

## Pre-task: Commit untracked M1 artifacts

The following files were created during M1 but not included in the M1 commits. Commit them before starting M2.

- `backend/domain/src/main/java/com/anomalydetection/domain/base/` (5 files: Entity, AggregateRoot, MultiTenant, FullAuditedEntity, BaseRepository)
- `backend/domain-shared/src/main/java/com/anomalydetection/shared/GuidGenerator.java`
- `docs/superpowers/plans/2026-04-25-m1-identity-multitenancy.md` (modified)

- [ ] **Step: Commit untracked M1 files**

```bash
git add backend/domain/src/main/java/com/anomalydetection/domain/base/
git add backend/domain-shared/src/main/java/com/anomalydetection/shared/GuidGenerator.java
git add docs/superpowers/plans/2026-04-25-m1-identity-multitenancy.md
git commit -m "chore(m1): commit domain base classes and GuidGenerator missed in M1 commits"
```

---

## Tasks

### Task 1: POM changes — add Spring Authorization Server dependency

**Files:**
- Modify: `backend/auth-server/pom.xml`
- Modify: `backend/db-migrator/pom.xml`
- Modify: `backend/host/pom.xml`

The Spring Boot 3.3.x BOM manages `spring-security-oauth2-authorization-server` via the Spring Security BOM. No version number is needed.

- [ ] **Step 1: Update auth-server/pom.xml**

Replace the entire file:

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

  <dependencies>
    <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-oauth2-authorization-server</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
  </dependencies>
</project>
```

- [ ] **Step 2: Add spring-security-oauth2-authorization-server to db-migrator/pom.xml**

Add inside `<dependencies>` (before `</dependencies>`):

```xml
    <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-oauth2-authorization-server</artifactId>
    </dependency>
```

- [ ] **Step 3: Add spring-security-test to host/pom.xml**

Add inside the `<dependencies>` block (after the existing test dependencies):

```xml
    <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-test</artifactId>
      <scope>test</scope>
    </dependency>
```

- [ ] **Step 4: Verify compilation**

```bash
cd backend && mvn compile -q
```

Expected: `BUILD SUCCESS` with no errors.

- [ ] **Step 5: Commit**

```bash
git add backend/auth-server/pom.xml backend/db-migrator/pom.xml backend/host/pom.xml
git commit -m "build(m2): add spring-security-oauth2-authorization-server and spring-security-test dependencies"
```

---

### Task 2: Liquibase migration — OAuth2 Authorization Server tables

Spring Authorization Server uses three tables for JDBC persistence. These must exist before the host application starts.

**Files:**
- Create: `backend/infrastructure/src/main/resources/db/changelog/003-oauth2-authorization-server.yaml`
- Modify: `backend/infrastructure/src/main/resources/db/changelog/db.changelog-master.yaml`

- [ ] **Step 1: Create 003-oauth2-authorization-server.yaml**

```yaml
databaseChangeLog:
  - logicalFilePath: db/changelog/003-oauth2-authorization-server.yaml

  - changeSet:
      id: 003-001-oauth2-registered-client
      author: m2-migration
      changes:
        - createTable:
            tableName: oauth2_registered_client
            columns:
              - column:
                  name: id
                  type: VARCHAR(100)
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: client_id
                  type: VARCHAR(100)
                  constraints:
                    nullable: false
              - column:
                  name: client_id_issued_at
                  type: TIMESTAMP
                  defaultValueComputed: CURRENT_TIMESTAMP
                  constraints:
                    nullable: false
              - column:
                  name: client_secret
                  type: VARCHAR(200)
              - column:
                  name: client_secret_expires_at
                  type: TIMESTAMP
              - column:
                  name: client_name
                  type: VARCHAR(200)
                  constraints:
                    nullable: false
              - column:
                  name: client_authentication_methods
                  type: VARCHAR(1000)
                  constraints:
                    nullable: false
              - column:
                  name: authorization_grant_types
                  type: VARCHAR(1000)
                  constraints:
                    nullable: false
              - column:
                  name: redirect_uris
                  type: VARCHAR(1000)
              - column:
                  name: post_logout_redirect_uris
                  type: VARCHAR(1000)
              - column:
                  name: scopes
                  type: VARCHAR(1000)
                  constraints:
                    nullable: false
              - column:
                  name: client_settings
                  type: VARCHAR(2000)
                  constraints:
                    nullable: false
              - column:
                  name: token_settings
                  type: VARCHAR(2000)
                  constraints:
                    nullable: false

  - changeSet:
      id: 003-002-oauth2-authorization
      author: m2-migration
      changes:
        - createTable:
            tableName: oauth2_authorization
            columns:
              - column:
                  name: id
                  type: VARCHAR(100)
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: registered_client_id
                  type: VARCHAR(100)
                  constraints:
                    nullable: false
              - column:
                  name: principal_name
                  type: VARCHAR(200)
                  constraints:
                    nullable: false
              - column:
                  name: authorization_grant_type
                  type: VARCHAR(100)
                  constraints:
                    nullable: false
              - column:
                  name: authorized_scopes
                  type: VARCHAR(1000)
              - column:
                  name: attributes
                  type: BLOB
              - column:
                  name: state
                  type: VARCHAR(500)
              - column:
                  name: authorization_code_value
                  type: BLOB
              - column:
                  name: authorization_code_issued_at
                  type: TIMESTAMP
              - column:
                  name: authorization_code_expires_at
                  type: TIMESTAMP
              - column:
                  name: authorization_code_metadata
                  type: BLOB
              - column:
                  name: access_token_value
                  type: BLOB
              - column:
                  name: access_token_issued_at
                  type: TIMESTAMP
              - column:
                  name: access_token_expires_at
                  type: TIMESTAMP
              - column:
                  name: access_token_metadata
                  type: BLOB
              - column:
                  name: access_token_type
                  type: VARCHAR(100)
              - column:
                  name: access_token_scopes
                  type: VARCHAR(1000)
              - column:
                  name: oidc_id_token_value
                  type: BLOB
              - column:
                  name: oidc_id_token_issued_at
                  type: TIMESTAMP
              - column:
                  name: oidc_id_token_expires_at
                  type: TIMESTAMP
              - column:
                  name: oidc_id_token_metadata
                  type: BLOB
              - column:
                  name: refresh_token_value
                  type: BLOB
              - column:
                  name: refresh_token_issued_at
                  type: TIMESTAMP
              - column:
                  name: refresh_token_expires_at
                  type: TIMESTAMP
              - column:
                  name: refresh_token_metadata
                  type: BLOB
              - column:
                  name: user_code_value
                  type: BLOB
              - column:
                  name: user_code_issued_at
                  type: TIMESTAMP
              - column:
                  name: user_code_expires_at
                  type: TIMESTAMP
              - column:
                  name: user_code_metadata
                  type: BLOB
              - column:
                  name: device_code_value
                  type: BLOB
              - column:
                  name: device_code_issued_at
                  type: TIMESTAMP
              - column:
                  name: device_code_expires_at
                  type: TIMESTAMP
              - column:
                  name: device_code_metadata
                  type: BLOB

  - changeSet:
      id: 003-003-oauth2-authorization-consent
      author: m2-migration
      changes:
        - createTable:
            tableName: oauth2_authorization_consent
            columns:
              - column:
                  name: registered_client_id
                  type: VARCHAR(100)
                  constraints:
                    nullable: false
              - column:
                  name: principal_name
                  type: VARCHAR(200)
                  constraints:
                    nullable: false
              - column:
                  name: authorities
                  type: VARCHAR(1000)
                  constraints:
                    nullable: false
        - addPrimaryKey:
            tableName: oauth2_authorization_consent
            columnNames: registered_client_id, principal_name
```

- [ ] **Step 2: Update db.changelog-master.yaml**

Replace the entire file:

```yaml
databaseChangeLog:
  - logicalFilePath: db/changelog/db.changelog-master.yaml

  - include:
      file: classpath:db/changelog/001-identity.yaml
  - include:
      file: classpath:db/changelog/002-multitenancy.yaml
  - include:
      file: classpath:db/changelog/003-oauth2-authorization-server.yaml
```

- [ ] **Step 3: Commit**

```bash
git add backend/infrastructure/src/main/resources/db/changelog/
git commit -m "feat(m2): add Liquibase migration for Spring Authorization Server tables"
```

---

### Task 3: RSA key generator utility

Generates a 2048-bit RSA key pair at startup. Spring Authorization Server uses it to sign JWTs.

**Files:**
- Create: `backend/auth-server/src/main/java/com/anomalydetection/authserver/RsaKeyGenerator.java`

- [ ] **Step 1: Create RsaKeyGenerator.java**

```java
package com.anomalydetection.authserver;

import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

final class RsaKeyGenerator {

  private RsaKeyGenerator() {}

  static RSAKey generate() {
    KeyPair keyPair;
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      keyPair = generator.generateKeyPair();
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to generate RSA key pair", ex);
    }
    return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
        .privateKey((RSAPrivateKey) keyPair.getPrivate())
        .keyID(UUID.randomUUID().toString())
        .build();
  }
}
```

- [ ] **Step 2: Verify it compiles**

```bash
cd backend && mvn compile -pl auth-server -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add backend/auth-server/src/main/java/com/anomalydetection/authserver/RsaKeyGenerator.java
git commit -m "feat(m2): add RSA key pair generator for JWT signing"
```

---

### Task 4: Authorization Server configuration

The main Spring Authorization Server configuration. Creates the `@Order(1)` security filter chain, JDBC-backed registration/authorization/consent stores, JWK source, JWT decoder, and token customizer.

**Files:**
- Create: `backend/auth-server/src/main/java/com/anomalydetection/authserver/AuthorizationServerConfig.java`

The `@Order(1)` chain intercepts all Spring AS protocol endpoints (configured by `OAuth2AuthorizationServerConfiguration.applyDefaultSecurity`): `/oauth2/authorize`, `/oauth2/token`, `/oauth2/introspect`, `/oauth2/revoke`, `/oauth2/jwks`, `/connect/logout`, `/userinfo`, `/.well-known/openid-configuration`.

The `JwtDecoder` bean defined here is also used by the `@Order(2)` resource server chain in `SecurityConfiguration` (picked up automatically by Spring from the context).

- [ ] **Step 1: Create AuthorizationServerConfig.java**

```java
package com.anomalydetection.authserver;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.consent.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.consent.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.jdbc.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
public class AuthorizationServerConfig {

  @Bean
  @Order(1)
  public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
      throws Exception {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
    http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
        .oidc(Customizer.withDefaults());
    http
        .exceptionHandling(
            ex ->
                ex.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
        .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));
    return http.build();
  }

  @Bean
  public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcRegisteredClientRepository(jdbcTemplate);
  }

  @Bean
  public OAuth2AuthorizationService authorizationService(
      JdbcTemplate jdbcTemplate, RegisteredClientRepository clientRepository) {
    return new JdbcOAuth2AuthorizationService(jdbcTemplate, clientRepository);
  }

  @Bean
  public OAuth2AuthorizationConsentService authorizationConsentService(
      JdbcTemplate jdbcTemplate, RegisteredClientRepository clientRepository) {
    return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, clientRepository);
  }

  @Bean
  public JWKSource<SecurityContext> jwkSource() {
    RSAKey rsaKey = RsaKeyGenerator.generate();
    return new ImmutableJWKSet<>(new JWKSet(rsaKey));
  }

  @Bean
  public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
  }

  @Bean
  public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder()
        .issuer("http://localhost:44397")
        .build();
  }

  @Bean
  public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
    return context -> {
      String principalName = context.getPrincipal().getName();
      if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
        context.getClaims().claim("name", principalName);
      }
      if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
        context.getClaims().claim("username", principalName);
      }
    };
  }

  static RegisteredClient buildSpaClient(String id) {
    return RegisteredClient.withId(id)
        .clientId("anomaly-detection-spa")
        .clientName("Anomaly Detection SPA")
        .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
        .redirectUri("http://localhost:5173/callback")
        .redirectUri("http://localhost:5173/silent-renew.html")
        .postLogoutRedirectUri("http://localhost:5173")
        .scope(OidcScopes.OPENID)
        .scope(OidcScopes.PROFILE)
        .scope(OidcScopes.EMAIL)
        .clientSettings(
            ClientSettings.builder()
                .requireProofKey(true)
                .requireAuthorizationConsent(false)
                .build())
        .tokenSettings(
            TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(1))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .reuseRefreshTokens(false)
                .build())
        .build();
  }
}
```

Note: `buildSpaClient` is package-private and also used by `SeedDataInitializer` in db-migrator — except db-migrator does NOT depend on auth-server module, so the seeder will duplicate this builder inline. See Task 6.

- [ ] **Step 2: Verify compilation**

```bash
cd backend && mvn compile -pl auth-server -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add backend/auth-server/src/main/java/com/anomalydetection/authserver/AuthorizationServerConfig.java
git commit -m "feat(m2): add Spring Authorization Server configuration with JDBC storage and JWT signing"
```

---

### Task 5: Update SecurityConfiguration — resource server + form login

Replace the M1 stub (which permitted all requests) with a real `@Order(2)` filter chain that:
1. Validates JWT Bearer tokens for API requests
2. Serves the form-login page so the authorization code flow can redirect to `/login`
3. Returns HTTP 401 (not a redirect) for unauthenticated calls to `/api/**`

**Files:**
- Modify: `backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/SecurityConfiguration.java`

- [ ] **Step 1: Replace SecurityConfiguration.java**

```java
package com.anomalydetection.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()))
        .exceptionHandling(
            ex ->
                ex.defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")));
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
```

Key points:
- `@EnableMethodSecurity` enables `@PreAuthorize` for future M3 permission checks
- `/actuator/**` is open for health probes
- `/api/**` unauthenticated → 401 (not redirect to login)
- Other paths unauthenticated → redirect to `/login` form (used by authorization code flow)
- CSRF disabled (REST API + JWT; form-login CSRF is handled by the AS chain)
- The `JwtDecoder` bean from `AuthorizationServerConfig` is picked up automatically

- [ ] **Step 2: Verify compilation**

```bash
cd backend && mvn compile -pl infrastructure -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add backend/infrastructure/src/main/java/com/anomalydetection/infrastructure/security/SecurityConfiguration.java
git commit -m "feat(m2): replace SecurityConfiguration stub with resource server + form-login filter chain"
```

---

### Task 6: Seed OAuth2 SPA client in DB migrator

The SPA client `anomaly-detection-spa` must exist in `oauth2_registered_client` before the host app starts. The DB migrator seeds it on first run.

**Files:**
- Modify: `backend/db-migrator/src/main/java/com/anomalydetection/dbmigrator/SeedDataInitializer.java`

- [ ] **Step 1: Update SeedDataInitializer.java**

Replace the entire file (adds `seedOAuth2SpaClient()` while keeping existing methods unchanged):

```java
package com.anomalydetection.dbmigrator;

import com.anomalydetection.domain.identity.Role;
import com.anomalydetection.domain.identity.RoleRepository;
import com.anomalydetection.domain.identity.User;
import com.anomalydetection.domain.identity.UserRepository;
import com.anomalydetection.domain.multitenancy.Tenant;
import com.anomalydetection.domain.multitenancy.TenantRepository;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeedDataInitializer implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(SeedDataInitializer.class);

  static final String DEFAULT_TENANT_NAME = "Default";
  static final String ADMIN_USERNAME = "admin";
  static final String ADMIN_ROLE_NAME = "admin";
  static final String SPA_CLIENT_ID = "anomaly-detection-spa";

  private final TenantRepository tenantRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JdbcTemplate jdbcTemplate;

  public SeedDataInitializer(
      TenantRepository tenantRepository,
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      JdbcTemplate jdbcTemplate) {
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedDefaultTenant();
    seedAdminRole();
    seedAdminUser();
    seedOAuth2SpaClient();
  }

  private void seedDefaultTenant() {
    if (tenantRepository.existsByNormalizedName(DEFAULT_TENANT_NAME.toUpperCase())) {
      log.info("Default tenant already exists - skipping");
      return;
    }
    var tenant = new Tenant(UUID.randomUUID(), DEFAULT_TENANT_NAME);
    tenantRepository.save(tenant);
    log.info("Created default tenant: {}", DEFAULT_TENANT_NAME);
  }

  private void seedAdminRole() {
    if (roleRepository.findByNormalizedName(ADMIN_ROLE_NAME.toUpperCase()).isPresent()) {
      log.info("Admin role already exists - skipping");
      return;
    }
    var role = new Role(UUID.randomUUID(), ADMIN_ROLE_NAME, ADMIN_ROLE_NAME.toUpperCase());
    role.setStatic(true);
    roleRepository.save(role);
    log.info("Created admin role");
  }

  private void seedAdminUser() {
    if (userRepository.findByNormalizedUserName(ADMIN_USERNAME.toUpperCase()).isPresent()) {
      log.info("Admin user already exists - skipping");
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
    log.info("Created admin user (password from ADMIN_PASSWORD env, default Admin@1234)");
  }

  private void seedOAuth2SpaClient() {
    var repo = new JdbcRegisteredClientRepository(jdbcTemplate);
    if (repo.findByClientId(SPA_CLIENT_ID) != null) {
      log.info("OAuth2 SPA client already exists - skipping");
      return;
    }
    RegisteredClient spaClient =
        RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(SPA_CLIENT_ID)
            .clientName("Anomaly Detection SPA")
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://localhost:5173/callback")
            .redirectUri("http://localhost:5173/silent-renew.html")
            .postLogoutRedirectUri("http://localhost:5173")
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .scope(OidcScopes.EMAIL)
            .clientSettings(
                ClientSettings.builder()
                    .requireProofKey(true)
                    .requireAuthorizationConsent(false)
                    .build())
            .tokenSettings(
                TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofHours(1))
                    .refreshTokenTimeToLive(Duration.ofDays(30))
                    .reuseRefreshTokens(false)
                    .build())
            .build();
    repo.save(spaClient);
    log.info("Created OAuth2 SPA client: {}", SPA_CLIENT_ID);
  }
}
```

- [ ] **Step 2: Verify db-migrator compiles**

```bash
cd backend && mvn compile -pl db-migrator -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add backend/db-migrator/src/main/java/com/anomalydetection/dbmigrator/SeedDataInitializer.java
git commit -m "feat(m2): seed OAuth2 SPA client (anomaly-detection-spa) in DB migrator"
```

---

### Task 7: Update application.yml — issuer URI

The issuer URI must match between the authorization server and resource server. Hard-coded for development; override via environment variable for other environments.

**Files:**
- Modify: `backend/host/src/main/resources/application.yml`

- [ ] **Step 1: Add issuer configuration to application.yml**

Add these lines at the end of the file:

```yaml
spring:
  security:
    oauth2:
      authorizationserver:
        issuer: ${AUTH_SERVER_ISSUER:http://localhost:44397}
```

The final `application.yml` should look like:

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
  security:
    oauth2:
      authorizationserver:
        issuer: ${AUTH_SERVER_ISSUER:http://localhost:44397}

server:
  port: 44397

management:
  endpoints:
    web:
      exposure:
        include: health, info, mappings
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

- [ ] **Step 2: Commit**

```bash
git add backend/host/src/main/resources/application.yml
git commit -m "feat(m2): add AUTH_SERVER_ISSUER configuration to application.yml"
```

---

### Task 8: Integration tests — OAuth2 endpoints + resource server protection

Two test classes verify that:
1. The AS protocol endpoints are reachable and return valid responses
2. API endpoints require JWT Bearer and respond correctly

Both use MariaDB4j (same as existing host tests) and MockMvc.

**Files:**
- Create: `backend/host/src/test/java/com/anomalydetection/host/auth/OAuth2EndpointsTest.java`
- Create: `backend/host/src/test/java/com/anomalydetection/host/auth/ResourceServerProtectionTest.java`

- [ ] **Step 1: Write the failing tests — OAuth2EndpointsTest.java**

```java
package com.anomalydetection.host.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anomalydetection.AnomalyDetectionApplication;
import com.anomalydetection.host.support.MariaDB4jExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class OAuth2EndpointsTest {

  @Autowired private MockMvc mockMvc;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void oidcDiscoveryEndpointReturnsConfiguration() throws Exception {
    mockMvc
        .perform(get("/.well-known/openid-configuration"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token_endpoint").exists())
        .andExpect(jsonPath("$.jwks_uri").exists())
        .andExpect(jsonPath("$.authorization_endpoint").exists());
  }

  @Test
  void jwksEndpointReturnsRsaPublicKey() throws Exception {
    mockMvc
        .perform(get("/oauth2/jwks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keys").isArray())
        .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
        .andExpect(jsonPath("$.keys[0].use").value("sig"));
  }
}
```

- [ ] **Step 2: Run OAuth2EndpointsTest to verify it fails**

```bash
cd backend && mvn test -pl host -Dtest=OAuth2EndpointsTest -q 2>&1 | tail -30
```

Expected: Tests fail (AS not yet wired into host classpath properly, or compilation errors before Task 4 is wired).

If tests already pass (the AS config is already wired via host → auth-server dependency), note that and continue.

- [ ] **Step 3: Write ResourceServerProtectionTest.java**

```java
package com.anomalydetection.host.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anomalydetection.AnomalyDetectionApplication;
import com.anomalydetection.host.support.MariaDB4jExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class ResourceServerProtectionTest {

  @Autowired private MockMvc mockMvc;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void apiEndpointReturns401WithoutToken() throws Exception {
    mockMvc
        .perform(get("/api/tenants"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void apiEndpointReturns200WithValidJwt() throws Exception {
    mockMvc
        .perform(get("/api/tenants").with(jwt()))
        .andExpect(status().isOk());
  }

  @Test
  void actuatorHealthIsPublic() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk());
  }
}
```

- [ ] **Step 4: Run all new tests**

```bash
cd backend && mvn test -pl host -Dtest="OAuth2EndpointsTest,ResourceServerProtectionTest" 2>&1 | tail -40
```

Expected output contains:
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0  (OAuth2EndpointsTest)
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0  (ResourceServerProtectionTest)
```

If `apiEndpointReturns200WithValidJwt` fails with 403 (Forbidden), check that `TenantsController` doesn't use `@PreAuthorize` yet (it shouldn't in M1). If it does, add `ROLE_USER` authority: `.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))`.

- [ ] **Step 5: Run full test suite to verify no regressions**

```bash
cd backend && mvn test -pl host 2>&1 | tail -20
```

Expected: All existing tests still pass (TenantFilterIsolationTest, UserDetailsServiceTest, UserAppServiceTest, TenantResolutionFilterTest, LiquibaseStartupTest, HealthEndpointTest, ArchitectureTest, ModularityTest).

- [ ] **Step 6: Commit**

```bash
git add backend/host/src/test/java/com/anomalydetection/host/auth/
git commit -m "test(m2): add integration tests for OAuth2 endpoints and resource server JWT protection"
```

---

### Task 9: Full build verification + git tag

- [ ] **Step 1: Full build with all tests**

```bash
cd backend && mvn verify 2>&1 | tail -30
```

Expected: `BUILD SUCCESS`. All modules compile and all tests pass.

- [ ] **Step 2: Run db-migrator to apply new schema to local MySQL**

```bash
cd backend && mvn spring-boot:run -pl db-migrator
```

Expected log output includes:
```
Liquibase: Successfully applied ... changeSet(s)
Created OAuth2 SPA client: anomaly-detection-spa
```

- [ ] **Step 3: Start the host application and verify AS endpoints**

```bash
cd backend && mvn spring-boot:run -pl host -Dspring-boot.run.profiles=local
```

In a second terminal:
```bash
curl -s http://localhost:44397/.well-known/openid-configuration | python -m json.tool
```

Expected: JSON response with `issuer`, `authorization_endpoint`, `token_endpoint`, `jwks_uri`.

```bash
curl -s http://localhost:44397/oauth2/jwks
```

Expected: JSON with `keys` array containing RSA public key.

```bash
curl -s http://localhost:44397/api/tenants
```

Expected: `401 Unauthorized` (or JSON error body).

- [ ] **Step 4: Commit any final cleanup and tag**

```bash
git add -A
git commit -m "feat(m2): complete M2 Spring Authorization Server — embedded OAuth2/OIDC, JWT resource server"
git tag m2-spring-authorization-server
```

---

## Checklist — Spec coverage

| Requirement (from CLAUDE.md §2.1) | Covered by |
|---|---|
| Spring Authorization Server (組み込み) | Task 4 `AuthorizationServerConfig` |
| OAuth2/OIDC 認可サーバ | Task 4 `@Order(1)` chain + OIDC enabled |
| リソースサーバ (JWT Bearer) | Task 5 `SecurityConfiguration` `@Order(2)` |
| OAuth2 / OIDC クライアント登録 (SPA) | Task 6 `SeedDataInitializer` |
| Liquibase migration (AS tables) | Task 2 `003-oauth2-authorization-server.yaml` |
| JWT 署名 (RSA) | Task 3 `RsaKeyGenerator` + Task 4 `JWKSource` |
| テスト (JUnit 5 + MariaDB4j) | Task 8 `OAuth2EndpointsTest` + `ResourceServerProtectionTest` |

## Notes for subsequent milestones

- **M3 (横断機能)**: Add `tenant_id` and `roles` claims to JWT by introducing a `UserInfoProvider` interface in `application-contracts`. The token customizer in `AuthorizationServerConfig` can then call it without cross-module dependency issues.
- **M5 (フロントエンド)**: Wire `oidc-client-ts` against `http://localhost:44397` with client ID `anomaly-detection-spa` and PKCE. Add `http://localhost:5173/callback` as redirect URI (already seeded in db-migrator).
- **Production**: Replace `RsaKeyGenerator.generate()` (ephemeral key) with keystore-backed key loaded from file. The `AuthorizationServerSettings` issuer should read from `${AUTH_SERVER_ISSUER}` env var.
