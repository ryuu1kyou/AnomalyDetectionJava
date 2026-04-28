package com.anomalydetection;

import com.anomalydetection.application.permissions.PermissionManager;
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
import org.springframework.context.annotation.Profile;
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
@Profile("local")
class LocalDevSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(LocalDevSeeder.class);

  private final TenantRepository tenantRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JdbcTemplate jdbcTemplate;
  private final PermissionManager permissionManager;

  LocalDevSeeder(
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
    seedSpaClient();
    seedAdminPermissions();
  }

  private void seedDefaultTenant() {
    if (tenantRepository.existsByNormalizedName("DEFAULT")) return;
    tenantRepository.save(new Tenant(UUID.randomUUID(), "Default"));
    log.info("[LocalDevSeeder] Created default tenant");
  }

  private void seedAdminRole() {
    if (roleRepository.findByNormalizedName("ADMIN").isPresent()) return;
    var role = new Role(UUID.randomUUID(), "admin", "ADMIN");
    role.setStatic(true);
    roleRepository.save(role);
    log.info("[LocalDevSeeder] Created admin role");
  }

  private void seedAdminUser() {
    if (userRepository.findByNormalizedUserName("ADMIN").isPresent()) return;
    String password = System.getenv().getOrDefault("ADMIN_PASSWORD", "Admin@1234");
    var user = new User(UUID.randomUUID(), "admin", "ADMIN");
    user.setEmail("admin@anomalydetection.local");
    user.setNormalizedEmail("ADMIN@ANOMALYDETECTION.LOCAL");
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setActive(true);
    user.setEmailConfirmed(true);
    userRepository.save(user);
    log.info("[LocalDevSeeder] Created admin user (password: {})", password);
  }

  private void seedSpaClient() {
    var repo = new JdbcRegisteredClientRepository(jdbcTemplate);
    if (repo.findByClientId("anomaly-detection-spa") != null) return;
    var client = RegisteredClient.withId(UUID.randomUUID().toString())
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
        .clientSettings(ClientSettings.builder()
            .requireProofKey(true)
            .requireAuthorizationConsent(false)
            .build())
        .tokenSettings(TokenSettings.builder()
            .accessTokenTimeToLive(Duration.ofHours(1))
            .refreshTokenTimeToLive(Duration.ofDays(30))
            .reuseRefreshTokens(false)
            .build())
        .build();
    repo.save(client);
    log.info("[LocalDevSeeder] Created OAuth2 SPA client");
  }

  private void seedAdminPermissions() {
    var perms = java.util.List.of(
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
    for (var perm : perms) {
      permissionManager.grantToRole(perm, "admin", null);
    }
    log.info("[LocalDevSeeder] Seeded {} permissions for admin role", perms.size());
  }
}
