package com.anomalydetection.infrastructure.cansspecification;

import com.anomalydetection.domain.cansspecification.CanSystemCategory;
import com.anomalydetection.domain.cansspecification.CanSystemCategoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCanSystemCategoryRepository
    extends CanSystemCategoryRepository, JpaRepository<CanSystemCategory, UUID> {
}