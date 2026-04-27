package com.anomalydetection.infrastructure.knowledgebase;

import com.anomalydetection.domain.knowledgebase.KnowledgeArticleComment;
import com.anomalydetection.domain.knowledgebase.KnowledgeArticleCommentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaKnowledgeArticleCommentRepository
    extends JpaRepository<KnowledgeArticleComment, UUID>, KnowledgeArticleCommentRepository {

  @Override
  List<KnowledgeArticleComment> findAllByArticleId(UUID articleId);
}
