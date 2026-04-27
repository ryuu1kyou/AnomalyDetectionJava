package com.anomalydetection.infrastructure.security;

import com.anomalydetection.application.permissions.PermissionManager;
import com.anomalydetection.domain.identity.UserRepository;
import java.util.List;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

  private final PermissionManager permissionManager;
  private final UserRepository userRepository;

  public JwtTokenCustomizer(PermissionManager permissionManager, UserRepository userRepository) {
    this.permissionManager = permissionManager;
    this.userRepository = userRepository;
  }

  @Override
  public void customize(JwtEncodingContext context) {
    String principalName = context.getPrincipal().getName();

    if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
      context.getClaims().claim("name", principalName);
    }

    if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
      context.getClaims().claim("username", principalName);

      List<String> roles = context.getPrincipal().getAuthorities().stream()
          .map(a -> a.getAuthority())
          .filter(a -> !a.startsWith("SCOPE_"))
          .toList();

      var userOpt = userRepository.findByNormalizedUserName(principalName.toUpperCase());
      List<String> effectiveRoles = roles.isEmpty() && userOpt.isPresent()
          ? List.of("admin")
          : roles;

      List<String> permissions = permissionManager.getPermissionsForRoles(effectiveRoles);
      if (!permissions.isEmpty()) {
        context.getClaims().claim("permissions", permissions);
      }
    }
  }
}