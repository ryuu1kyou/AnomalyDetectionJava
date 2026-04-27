package com.anomalydetection.contracts.knowledgebase;

import com.anomalydetection.domain.knowledgebase.KnowledgeCategory;

public record GetKnowledgeArticlesInput(
    String filter,
    KnowledgeCategory category,
    Boolean isPublished,
    String detectionLogicId,
    String canSignalId,
    Integer skipCount,
    Integer maxResultCount) {}
