package com.anomalydetection.contracts.safety;

import com.anomalydetection.domain.safety.DecisionStatus;
import java.util.List;

public record DecisionLedgerDto(
    String id,
    String decisionId,
    String whatDecided,
    String whyDecided,
    String assumptions,
    String constraintsText,
    List<String> relatedFeatureIds,
    List<String> relatedModuleIds,
    DecisionStatus status,
    String approvedBy,
    String approvedAt) {}
