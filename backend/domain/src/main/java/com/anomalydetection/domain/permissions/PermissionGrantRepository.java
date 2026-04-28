package com.anomalydetection.domain.permissions;

import java.util.List;

public interface PermissionGrantRepository {

  PermissionGrant save(PermissionGrant grant);

  List<PermissionGrant> findByProviderNameAndProviderKey(String providerName, String providerKey);

  boolean existsByNameAndProviderNameAndProviderKey(
      String name, String providerName, String providerKey);

  List<PermissionGrant> findAll();

  List<PermissionGrant> findByProviderNameIn(List<String> providerNames);

  void deleteByNameAndProviderNameAndProviderKey(String name, String providerName, String providerKey);
}