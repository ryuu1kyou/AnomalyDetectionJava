package com.anomalydetection.infrastructure.security;

import com.anomalydetection.shared.CurrentUserIdHolder;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextUserIdProvider implements CurrentUserIdHolder.UserIdProvider {

  @PostConstruct
  public void register() {
    CurrentUserIdHolder.setProvider(this);
  }

  @Override
  public Optional<UUID> getUserId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = jwtAuth.getToken();
      String sub = jwt.getSubject();
      if (sub != null) {
        try {
          return Optional.of(UUID.fromString(sub));
        } catch (IllegalArgumentException ignored) {}
      }
    }
    return Optional.empty();
  }
}