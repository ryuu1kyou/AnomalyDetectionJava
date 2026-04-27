package com.anomalydetection.shared;

import java.util.Optional;
import java.util.UUID;

/**
 * Static bridge that lets domain entities read the current user ID without
 * depending on Spring Security. Infrastructure wires in a provider on startup.
 */
public final class CurrentUserIdHolder {

  private CurrentUserIdHolder() {}

  @FunctionalInterface
  public interface UserIdProvider {
    Optional<UUID> getUserId();
  }

  private static volatile UserIdProvider provider;

  public static void setProvider(UserIdProvider p) {
    provider = p;
  }

  public static Optional<UUID> getUserId() {
    return provider != null ? provider.getUserId() : Optional.empty();
  }
}