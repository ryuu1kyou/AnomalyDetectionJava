package com.anomalydetection.infrastructure.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

interface JpaAuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {}