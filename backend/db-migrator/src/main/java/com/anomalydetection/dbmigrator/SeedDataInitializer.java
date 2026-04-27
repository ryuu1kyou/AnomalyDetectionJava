package com.anomalydetection.dbmigrator;

import com.anomalydetection.domain.identity.Role;
import com.anomalydetection.domain.identity.RoleRepository;
import com.anomalydetection.domain.identity.User;
import com.anomalydetection.domain.identity.UserRepository;
import com.anomalydetection.domain.multitenancy.Tenant;
import com.anomalydetection.application.permissions.PermissionManager;
import com.anomalydetection.domain.multitenancy.TenantRepository;
import java.time.Duration;
import java.util.List;
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
  private final PermissionManager permissionManager;

  public SeedDataInitializer(
      TenantRepository tenantRepository,
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      JdbcTemplate jdbcTemplate,
      PermissionManager permissionManager) {
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jdbcTemplate = jdbcTemplate;
    this.permissionManager = permissionManager;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedDefaultTenant();
    seedAdminRole();
    seedAdminUser();
    seedOAuth2SpaClient();
    seedAdminRolePermissions();
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

  private void seedAdminRolePermissions() {
    var allPermissions = List.of(
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
      permissionManager.grantToRole(perm, ADMIN_ROLE_NAME, null);
    }
    log.info("Seeded {} permissions for admin role", allPermissions.size());
  }
}
