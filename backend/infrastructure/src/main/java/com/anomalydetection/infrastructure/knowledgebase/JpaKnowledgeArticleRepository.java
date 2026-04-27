package com.anomalydetection.infrastructure.knowledgebase;

import com.anomalydetection.domain.knowledgebase.KnowledgeArticle;
import com.anomalydetection.domain.knowledgebase.KnowledgeArticleRepository;
import com.anomalydetection.domain.knowledgebase.KnowledgeCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaKnowledgeArticleRepository
    extends JpaRepository<KnowledgeArticle, UUID>, KnowledgeArticleRepository {

  @Override
  List<KnowledgeArticle> findAllByIsPublished(boolean isPublished);

  @Override
  List<KnowledgeArticle> findAllByCategory(KnowledgeCategory category);

  @Override
  List<KnowledgeArticle> findAllByDetectionLogicId(UUID detectionLogicId);

  @Override
  List<KnowledgeArticle> findAllByCanSignalId(UUID canSignalId);
}
