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
