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
