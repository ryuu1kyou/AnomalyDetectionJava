package com.anomalydetection.infrastructure.safety;

import com.anomalydetection.domain.safety.DecisionLedger;
import com.anomalydetection.domain.safety.DecisionLedgerRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDecisionLedgerRepository
    extends DecisionLedgerRepository, JpaRepository<DecisionLedger, UUID> {}
