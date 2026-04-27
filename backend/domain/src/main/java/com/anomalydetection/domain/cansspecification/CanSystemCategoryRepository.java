package com.anomalydetection.domain.cansspecification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CanSystemCategoryRepository {
  CanSystemCategory save(CanSystemCategory category);
  Optional<CanSystemCategory> findById(UUID id);
  List<CanSystemCategory> findAll();
  boolean existsById(UUID id);
  void deleteById(UUID id);
}