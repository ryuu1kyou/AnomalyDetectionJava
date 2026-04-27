package com.anomalydetection.contracts.knowledgebase;

import com.anomalydetection.domain.knowledgebase.KnowledgeCategory;
import java.util.List;

public record UpdateKnowledgeArticleDto(
    String title,
    String content,
    String summary,
    KnowledgeCategory category,
    List<String> tags,
    String detectionLogicId,
    String canSignalId,
    String anomalyType,
    String signalName,
    String symptom,
    String cause,
    String countermeasure,
    boolean hasSolution,
    List<String> solutionSteps,
    List<String> preventionMeasures) {}
