package com.anomalydetection.domain.permissions;

import java.util.List;

public interface PermissionGrantRepository {

  PermissionGrant save(PermissionGrant grant);

  List<PermissionGrant> findByProviderNameAndProviderKey(String providerName, String providerKey);

  boolean existsByNameAndProviderNameAndProviderKey(
      String name, String providerName, String providerKey);

  List<PermissionGrant> findByProviderNameIn(List<String> providerNames);
}