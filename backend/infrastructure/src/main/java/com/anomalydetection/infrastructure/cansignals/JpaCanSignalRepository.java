package com.anomalydetection.infrastructure.cansignals;

import com.anomalydetection.domain.cansignals.CanSignal;
import com.anomalydetection.domain.cansignals.CanSignalRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCanSignalRepository
    extends CanSignalRepository, JpaRepository<CanSignal, UUID> {

  List<CanSignal> findByFrameId(int frameId);
}