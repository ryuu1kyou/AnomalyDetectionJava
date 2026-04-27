package com.anomalydetection.contracts.knowledgebase;

public record CreateKnowledgeArticleCommentDto(
    String articleId,
    String authorName,
    String content,
    int rating) {}
