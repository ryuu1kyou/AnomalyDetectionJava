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
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }
}
