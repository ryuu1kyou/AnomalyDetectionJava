package com.anomalydetection.domain.safety;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DecisionLedgerRepository extends BaseRepository<DecisionLedger, UUID> {

  Optional<DecisionLedger> findByDecisionId(String decisionId);

  List<DecisionLedger> findAllByStatus(DecisionStatus status);

  boolean existsByDecisionId(String decisionId);
}
