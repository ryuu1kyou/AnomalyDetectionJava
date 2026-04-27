package com.anomalydetection.contracts.knowledgebase;

import com.anomalydetection.domain.knowledgebase.KnowledgeCategory;
import java.util.List;

public record KnowledgeArticleDto(
    String id,
    String title,
    String content,
    String summary,
    KnowledgeCategory category,
    List<String> tags,
    int viewCount,
    int usefulCount,
    boolean isPublished,
    String publishedAt,
    String detectionLogicId,
    String canSignalId,
    String anomalyType,
    String signalName,
    String symptom,
    String cause,
    String countermeasure,
    boolean hasSolution,
    List<String> solutionSteps,
    List<String> preventionMeasures,
    double averageRating,
    int ratingCount) {}
