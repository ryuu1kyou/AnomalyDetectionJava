package com.anomalydetection.domain.knowledgebase;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface KnowledgeArticleRepository extends BaseRepository<KnowledgeArticle, UUID> {

  List<KnowledgeArticle> findAllByIsPublished(boolean isPublished);

  List<KnowledgeArticle> findAllByCategory(KnowledgeCategory category);

  List<KnowledgeArticle> findAllByDetectionLogicId(UUID detectionLogicId);

  List<KnowledgeArticle> findAllByCanSignalId(UUID canSignalId);
}
