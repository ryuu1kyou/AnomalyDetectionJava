package com.anomalydetection.domain.cansignals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CanSignalRepository {
  CanSignal save(CanSignal signal);
  Optional<CanSignal> findById(UUID id);
  List<CanSignal> findAll();
  List<CanSignal> findByFrameId(int frameId);
  boolean existsById(UUID id);
  void deleteById(UUID id);
}