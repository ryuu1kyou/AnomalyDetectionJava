package com.anomalydetection.contracts.safety;

import java.util.List;

public record UpdateDecisionLedgerDto(
    String whatDecided,
    String whyDecided,
    String assumptions,
    String constraintsText,
    List<String> relatedFeatureIds,
    List<String> relatedModuleIds) {}
