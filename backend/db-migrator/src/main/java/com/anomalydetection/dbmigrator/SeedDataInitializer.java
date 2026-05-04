package com.anomalydetection.dbmigrator;

import com.anomalydetection.application.permissions.PermissionManager;
import com.anomalydetection.contracts.anomalydetection.AnomalyDetectionPermissions;
import com.anomalydetection.contracts.cansignals.CanSignalPermissions;
import com.anomalydetection.contracts.cansspecification.CanSpecificationPermissions;
import com.anomalydetection.contracts.detectiontemplates.DetectionTemplatePermissions;
import com.anomalydetection.contracts.identity.IdentityPermissions;
import com.anomalydetection.contracts.integration.IntegrationPermissions;
import com.anomalydetection.contracts.knowledgebase.KnowledgeBasePermissions;
import com.anomalydetection.contracts.oemtraceability.OemTraceabilityPermissions;
import com.anomalydetection.contracts.projects.ProjectPermissions;
import com.anomalydetection.contracts.safety.DecisionLedgerPermissions;
import com.anomalydetection.contracts.safety.SafetyTracePermissions;
import com.anomalydetection.contracts.similarpatternsearch.SimilarPatternSearchPermissions;
import com.anomalydetection.domain.cansspecification.CanSystemCategory;
import com.anomalydetection.domain.cansspecification.CanSystemCategoryRepository;
import com.anomalydetection.domain.detectiontemplates.DetectionTemplate;
import com.anomalydetection.domain.detectiontemplates.DetectionTemplateRepository;
import com.anomalydetection.domain.identity.Role;
import com.anomalydetection.domain.identity.RoleRepository;
import com.anomalydetection.domain.identity.User;
import com.anomalydetection.domain.identity.UserRepository;
import com.anomalydetection.domain.multitenancy.Tenant;
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
  private final CanSystemCategoryRepository canSystemCategoryRepository;
  private final DetectionTemplateRepository detectionTemplateRepository;

  public SeedDataInitializer(
      TenantRepository tenantRepository,
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      JdbcTemplate jdbcTemplate,
      PermissionManager permissionManager,
      CanSystemCategoryRepository canSystemCategoryRepository,
      DetectionTemplateRepository detectionTemplateRepository) {
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jdbcTemplate = jdbcTemplate;
    this.permissionManager = permissionManager;
    this.canSystemCategoryRepository = canSystemCategoryRepository;
    this.detectionTemplateRepository = detectionTemplateRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedDefaultTenant();
    seedAdminRole();
    seedAdminUser();
    seedOAuth2SpaClient();
    seedAdminRolePermissions();
    seedCanSystemCategories();
    seedDefaultDetectionTemplates();
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
        // Identity
        IdentityPermissions.USERS_VIEW,
        IdentityPermissions.USERS_CREATE,
        IdentityPermissions.USERS_EDIT,
        IdentityPermissions.USERS_DELETE,
        IdentityPermissions.ROLES_VIEW,
        IdentityPermissions.ROLES_CREATE,
        IdentityPermissions.ROLES_EDIT,
        IdentityPermissions.ROLES_DELETE,
        IdentityPermissions.TENANTS_VIEW,
        IdentityPermissions.TENANTS_CREATE,
        IdentityPermissions.TENANTS_EDIT,
        IdentityPermissions.TENANTS_DELETE,
        // CanSignal
        CanSignalPermissions.DEFAULT,
        CanSignalPermissions.CREATE,
        CanSignalPermissions.EDIT,
        CanSignalPermissions.DELETE,
        // CanSpecification
        CanSpecificationPermissions.DEFAULT,
        CanSpecificationPermissions.CREATE,
        CanSpecificationPermissions.EDIT,
        CanSpecificationPermissions.DELETE,
        // DetectionTemplates
        DetectionTemplatePermissions.DEFAULT,
        DetectionTemplatePermissions.CREATE,
        DetectionTemplatePermissions.EDIT,
        DetectionTemplatePermissions.DELETE,
        // AnomalyDetection (Logic + Result)
        AnomalyDetectionPermissions.LOGIC_DEFAULT,
        AnomalyDetectionPermissions.LOGIC_CREATE,
        AnomalyDetectionPermissions.LOGIC_EDIT,
        AnomalyDetectionPermissions.LOGIC_DELETE,
        AnomalyDetectionPermissions.LOGIC_APPROVE,
        AnomalyDetectionPermissions.RESULT_DEFAULT,
        AnomalyDetectionPermissions.RESULT_CREATE,
        AnomalyDetectionPermissions.RESULT_EDIT,
        AnomalyDetectionPermissions.RESULT_DELETE,
        // Projects
        ProjectPermissions.DEFAULT,
        ProjectPermissions.CREATE,
        ProjectPermissions.EDIT,
        ProjectPermissions.DELETE,
        ProjectPermissions.MANAGE_MEMBERS,
        ProjectPermissions.MANAGE_MILESTONES,
        // Safety
        SafetyTracePermissions.DEFAULT,
        SafetyTracePermissions.CREATE,
        SafetyTracePermissions.EDIT,
        SafetyTracePermissions.DELETE,
        SafetyTracePermissions.APPROVE,
        SafetyTracePermissions.AUDIT_EXPORT,
        // Decision Ledger (M9-A)
        DecisionLedgerPermissions.DEFAULT,
        DecisionLedgerPermissions.CREATE,
        DecisionLedgerPermissions.EDIT,
        DecisionLedgerPermissions.DELETE,
        DecisionLedgerPermissions.APPROVE,
        // KnowledgeBase
        KnowledgeBasePermissions.DEFAULT,
        KnowledgeBasePermissions.CREATE,
        KnowledgeBasePermissions.EDIT,
        KnowledgeBasePermissions.DELETE,
        KnowledgeBasePermissions.PUBLISH,
        // OemTraceability
        OemTraceabilityPermissions.APPROVAL_DEFAULT,
        OemTraceabilityPermissions.APPROVAL_CREATE,
        OemTraceabilityPermissions.APPROVAL_MANAGE,
        OemTraceabilityPermissions.CUSTOMIZATION_DEFAULT,
        OemTraceabilityPermissions.CUSTOMIZATION_CREATE,
        OemTraceabilityPermissions.CUSTOMIZATION_MANAGE,
        // SimilarPatternSearch
        SimilarPatternSearchPermissions.DEFAULT,
        SimilarPatternSearchPermissions.SEARCH_SIGNALS,
        SimilarPatternSearchPermissions.COMPARE_TEST_DATA,
        // Integration
        IntegrationPermissions.DEFAULT,
        IntegrationPermissions.CREATE,
        IntegrationPermissions.MANAGE,
        IntegrationPermissions.IMPORT_DATA
    );
    for (String perm : allPermissions) {
      permissionManager.grantToRole(perm, ADMIN_ROLE_NAME, null);
    }
    log.info("Seeded {} permissions for admin role", allPermissions.size());
  }

  private void seedCanSystemCategories() {
    if (canSystemCategoryRepository.count() > 0) {
      log.info("CanSystemCategory master data already exists - skipping");
      return;
    }
    var categories = List.of(
        createCategory("Engine Control", "Engine control unit signals", 1),
        createCategory("Transmission", "Transmission control signals", 2),
        createCategory("Brake System", "ABS/ESC brake system signals", 3),
        createCategory("Body Control", "Body control module signals", 4),
        createCategory("ADAS", "Advanced driver-assistance system signals", 5),
        createCategory("Powertrain", "Powertrain management signals", 6),
        createCategory("Chassis", "Chassis and suspension signals", 7),
        createCategory("Infotainment", "Infotainment and HMI signals", 8)
    );
    categories.forEach(canSystemCategoryRepository::save);
    log.info("Seeded {} CanSystemCategory master records", categories.size());
  }

  private CanSystemCategory createCategory(String name, String description, int order) {
    var cat = new CanSystemCategory(UUID.randomUUID(), name);
    cat.setDescription(description);
    cat.setDisplayOrder(order);
    return cat;
  }

  private void seedDefaultDetectionTemplates() {
    if (detectionTemplateRepository.count() > 0) {
      log.info("DetectionTemplate seed data already exists - skipping");
      return;
    }
    var templates = List.of(
        createTemplate(
            "Threshold Exceeded",
            "Triggers when a signal value exceeds a configured threshold",
            "value > threshold",
            100.0),
        createTemplate(
            "Out of Range",
            "Triggers when a signal value falls outside the expected range",
            "value < min || value > max",
            null),
        createTemplate(
            "Rapid Change",
            "Triggers when a signal changes faster than the allowed rate",
            "abs(delta) > rateLimit",
            null),
        createTemplate(
            "Signal Lost",
            "Triggers when no signal update is received within the timeout period",
            "elapsed > timeout",
            null)
    );
    templates.forEach(detectionTemplateRepository::save);
    log.info("Seeded {} default DetectionTemplate records", templates.size());
  }

  private DetectionTemplate createTemplate(String name, String description, String expression,
      Double threshold) {
    var t = new DetectionTemplate(UUID.randomUUID(), name);
    t.setDescription(description);
    t.setExpression(expression);
    t.setThreshold(threshold);
    return t;
  }
}
