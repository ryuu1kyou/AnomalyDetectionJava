package com.anomalydetection.contracts.knowledgebase;

public record KnowledgeArticleCommentDto(
    String id,
    String articleId,
    String authorUserId,
    String authorName,
    String content,
    int rating,
    String createdAt) {}
