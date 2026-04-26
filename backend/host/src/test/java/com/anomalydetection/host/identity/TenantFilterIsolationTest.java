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
    userA.setTenantId(tenantA);
    userRepository.save(userA);

    // Create user in tenant B
    tenantHolder.setTenantId(tenantB);
    User userB = new User(UUID.randomUUID(), "bob", "BOB");
    userB.setTenantId(tenantB);
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
