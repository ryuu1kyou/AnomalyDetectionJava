package com.anomalydetection.infrastructure.cansspecification;

import com.anomalydetection.domain.cansspecification.CanSignalSpecification;
import com.anomalydetection.domain.cansspecification.CanSignalSpecificationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCanSignalSpecificationRepository
    extends CanSignalSpecificationRepository, JpaRepository<CanSignalSpecification, UUID> {

  List<CanSignalSpecification> findBySystemCategoryId(UUID systemCategoryId);
}