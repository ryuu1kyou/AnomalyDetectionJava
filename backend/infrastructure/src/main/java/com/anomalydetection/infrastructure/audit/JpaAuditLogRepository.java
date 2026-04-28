package com.anomalydetection.infrastructure.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaAuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {
  List<AuditLogEntity> findAllByOrderByOccurredAtDesc(Pageable pageable);
}