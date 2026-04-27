package com.anomalydetection.domain.cansspecification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CanSignalSpecificationRepository {
  CanSignalSpecification save(CanSignalSpecification spec);
  Optional<CanSignalSpecification> findById(UUID id);
  List<CanSignalSpecification> findAll();
  List<CanSignalSpecification> findBySystemCategoryId(UUID systemCategoryId);
  boolean existsById(UUID id);
  void deleteById(UUID id);
}