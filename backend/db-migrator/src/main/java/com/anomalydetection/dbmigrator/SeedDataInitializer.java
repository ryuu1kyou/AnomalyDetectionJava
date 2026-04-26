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
}
