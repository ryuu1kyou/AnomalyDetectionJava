package com.anomalydetection.domain.knowledgebase;

import com.anomalydetection.domain.base.BaseRepository;
import java.util.List;
import java.util.UUID;

public interface KnowledgeArticleCommentRepository
    extends BaseRepository<KnowledgeArticleComment, UUID> {

  List<KnowledgeArticleComment> findAllByArticleId(UUID articleId);
}
