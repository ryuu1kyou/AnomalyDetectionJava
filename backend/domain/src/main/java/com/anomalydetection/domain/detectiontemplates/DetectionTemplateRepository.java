package com.anomalydetection.domain.detectiontemplates;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DetectionTemplateRepository {

  DetectionTemplate save(DetectionTemplate template);

  Optional<DetectionTemplate> findById(UUID id);

  List<DetectionTemplate> findAll();

  boolean existsById(UUID id);

  long count();

  void deleteById(UUID id);
}
