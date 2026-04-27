package com.anomalydetection.infrastructure.detectiontemplates;

import com.anomalydetection.domain.detectiontemplates.DetectionTemplate;
import com.anomalydetection.domain.detectiontemplates.DetectionTemplateRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDetectionTemplateRepository
    extends DetectionTemplateRepository, JpaRepository<DetectionTemplate, UUID> {}
