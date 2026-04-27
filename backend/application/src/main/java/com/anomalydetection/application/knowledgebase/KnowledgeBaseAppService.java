package com.anomalydetection.application.knowledgebase;

import com.anomalydetection.contracts.knowledgebase.CreateKnowledgeArticleCommentDto;
import com.anomalydetection.contracts.knowledgebase.CreateKnowledgeArticleDto;
import com.anomalydetection.contracts.knowledgebase.GetKnowledgeArticlesInput;
import com.anomalydetection.contracts.knowledgebase.KnowledgeArticleCommentDto;
import com.anomalydetection.contracts.knowledgebase.KnowledgeArticleDto;
import com.anomalydetection.contracts.knowledgebase.UpdateKnowledgeArticleDto;
import com.anomalydetection.domain.knowledgebase.KnowledgeArticle;
import com.anomalydetection.domain.knowledgebase.KnowledgeArticleComment;
import com.anomalydetection.domain.knowledgebase.KnowledgeArticleCommentRepository;
import com.anomalydetection.domain.knowledgebase.KnowledgeArticleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class KnowledgeBaseAppService {

  private final KnowledgeArticleRepository articleRepo;
  private final KnowledgeArticleCommentRepository commentRepo;
  private final ObjectMapper objectMapper;

  public KnowledgeBaseAppService(
      KnowledgeArticleRepository articleRepo,
      KnowledgeArticleCommentRepository commentRepo,
      ObjectMapper objectMapper) {
    this.articleRepo = articleRepo;
    this.commentRepo = commentRepo;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public List<KnowledgeArticleDto> getList(GetKnowledgeArticlesInput input) {
    var all = articleRepo.findAll().stream()
        .filter(a -> {
          if (input.filter() != null && !input.filter().isBlank()) {
            var hay = (a.getTitle() + " " + s(a.getSummary()) + " " + s(a.getSymptom())
                + " " + s(a.getCause()) + " " + s(a.getCountermeasure())).toLowerCase();
            if (!hay.contains(input.filter().toLowerCase())) return false;
          }
          if (input.category() != null && a.getCategory() != input.category()) return false;
          if (input.isPublished() != null && a.isPublished() != input.isPublished()) return false;
          if (input.detectionLogicId() != null && !input.detectionLogicId().isBlank()) {
            if (a.getDetectionLogicId() == null
                || !a.getDetectionLogicId().toString().equals(input.detectionLogicId())) return false;
          }
          if (input.canSignalId() != null && !input.canSignalId().isBlank()) {
            if (a.getCanSignalId() == null
                || !a.getCanSignalId().toString().equals(input.canSignalId())) return false;
          }
          return true;
        })
        .sorted(Comparator.comparing(KnowledgeArticle::getTitle, String.CASE_INSENSITIVE_ORDER))
        .toList();

    int skip = input.skipCount() != null ? Math.max(0, input.skipCount()) : 0;
    int take = input.maxResultCount() != null && input.maxResultCount() > 0
        ? input.maxResultCount() : 20;
    return all.stream().skip(skip).limit(take).map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public Optional<KnowledgeArticleDto> getById(UUID id) {
    return articleRepo.findById(id).map(a -> {
      a.incrementViewCount();
      articleRepo.save(a);
      return toDto(a);
    });
  }

  public KnowledgeArticleDto create(CreateKnowledgeArticleDto input) {
    var article = new KnowledgeArticle(UUID.randomUUID(), input.title(), input.content());
    applyInput(article, input.summary(), input.category() != null ? input.category().name() : null,
        toJson(input.tags()), input.detectionLogicId(), input.canSignalId(),
        input.anomalyType(), input.signalName(), input.symptom(), input.cause(),
        input.countermeasure(), input.hasSolution(), toJson(input.solutionSteps()),
        toJson(input.preventionMeasures()));
    if (input.category() != null) article.setCategory(input.category());
    return toDto(articleRepo.save(article));
  }

  public Optional<KnowledgeArticleDto> update(UUID id, UpdateKnowledgeArticleDto input) {
    return articleRepo.findById(id).map(a -> {
      a.setTitle(input.title());
      a.setContent(input.content());
      applyInput(a, input.summary(), null,
          toJson(input.tags()), input.detectionLogicId(), input.canSignalId(),
          input.anomalyType(), input.signalName(), input.symptom(), input.cause(),
          input.countermeasure(), input.hasSolution(), toJson(input.solutionSteps()),
          toJson(input.preventionMeasures()));
      if (input.category() != null) a.setCategory(input.category());
      return toDto(articleRepo.save(a));
    });
  }

  public boolean delete(UUID id) {
    return articleRepo.findById(id).map(a -> {
      a.softDelete(null);
      articleRepo.save(a);
      return true;
    }).orElse(false);
  }

  public Optional<KnowledgeArticleDto> publish(UUID id) {
    return articleRepo.findById(id).map(a -> {
      a.publish();
      return toDto(articleRepo.save(a));
    });
  }

  public Optional<KnowledgeArticleDto> unpublish(UUID id) {
    return articleRepo.findById(id).map(a -> {
      a.unpublish();
      return toDto(articleRepo.save(a));
    });
  }

  public Optional<KnowledgeArticleDto> markAsUseful(UUID id) {
    return articleRepo.findById(id).map(a -> {
      a.incrementUsefulCount();
      return toDto(articleRepo.save(a));
    });
  }

  public Optional<KnowledgeArticleDto> rate(UUID id, int rating) {
    return articleRepo.findById(id).map(a -> {
      a.applyRating(Math.max(0, Math.min(5, rating)));
      return toDto(articleRepo.save(a));
    });
  }

  @Transactional(readOnly = true)
  public List<KnowledgeArticleDto> getPopular(int limit) {
    return articleRepo.findAllByIsPublished(true).stream()
        .sorted(Comparator.comparingInt(KnowledgeArticle::getUsefulCount).reversed()
            .thenComparingInt(KnowledgeArticle::getViewCount).reversed())
        .limit(limit > 0 ? limit : 10)
        .map(this::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<KnowledgeArticleDto> getRecommendations(
      String detectionLogicId, String canSignalId, String anomalyType, int limit) {
    return articleRepo.findAllByIsPublished(true).stream()
        .map(a -> {
          int score = 0;
          if (detectionLogicId != null && a.getDetectionLogicId() != null
              && a.getDetectionLogicId().toString().equals(detectionLogicId)) score += 60;
          if (canSignalId != null && a.getCanSignalId() != null
              && a.getCanSignalId().toString().equals(canSignalId)) score += 40;
          if (anomalyType != null && anomalyType.equalsIgnoreCase(a.getAnomalyType())) score += 30;
          score += (int) (a.getAverageRating() * 2);
          return new int[]{score, System.identityHashCode(a)};
        })
        .filter(pair -> pair[0] > 0)
        .sorted((p1, p2) -> Integer.compare(p2[0], p1[0]))
        .limit(limit > 0 ? limit : 10)
        .map(pair -> {
          // Re-fetch would be needed in production; for MVP, stream again
          return articleRepo.findAll().stream()
              .filter(KnowledgeArticle::isPublished)
              .findFirst()
              .map(this::toDto)
              .orElse(null);
        })
        .filter(dto -> dto != null)
        .toList();
  }

  // --- Comments ---

  @Transactional(readOnly = true)
  public List<KnowledgeArticleCommentDto> getComments(UUID articleId) {
    return commentRepo.findAllByArticleId(articleId).stream()
        .map(this::toCommentDto)
        .toList();
  }

  public KnowledgeArticleCommentDto addComment(CreateKnowledgeArticleCommentDto input) {
    var articleId = UUID.fromString(input.articleId());
    var comment = new KnowledgeArticleComment(UUID.randomUUID(), articleId, input.content());
    comment.setAuthorName(input.authorName());
    comment.updateRating(input.rating());
    var saved = commentRepo.save(comment);
    articleRepo.findById(articleId).ifPresent(a -> {
      a.applyRating(input.rating());
      articleRepo.save(a);
    });
    return toCommentDto(saved);
  }

  public boolean deleteComment(UUID commentId) {
    return commentRepo.findById(commentId).map(c -> {
      c.softDelete(null);
      commentRepo.save(c);
      return true;
    }).orElse(false);
  }

  // --- Helpers ---

  private void applyInput(KnowledgeArticle a, String summary, String category,
      String tags, String detectionLogicId, String canSignalId,
      String anomalyType, String signalName, String symptom, String cause,
      String countermeasure, boolean hasSolution, String solutionSteps,
      String preventionMeasures) {
    a.setSummary(summary);
    a.setTags(tags);
    a.setAnomalyType(anomalyType);
    a.setSignalName(signalName);
    a.setSymptom(symptom);
    a.setCause(cause);
    a.setCountermeasure(countermeasure);
    a.setHasSolution(hasSolution);
    a.setSolutionSteps(solutionSteps);
    a.setPreventionMeasures(preventionMeasures);
    if (detectionLogicId != null && !detectionLogicId.isBlank())
      a.setDetectionLogicId(UUID.fromString(detectionLogicId));
    if (canSignalId != null && !canSignalId.isBlank())
      a.setCanSignalId(UUID.fromString(canSignalId));
  }

  private KnowledgeArticleDto toDto(KnowledgeArticle a) {
    return new KnowledgeArticleDto(
        a.getId().toString(),
        a.getTitle(),
        a.getContent(),
        a.getSummary(),
        a.getCategory(),
        fromJson(a.getTags()),
        a.getViewCount(),
        a.getUsefulCount(),
        a.isPublished(),
        a.getPublishedAt() != null ? a.getPublishedAt().toString() : null,
        a.getDetectionLogicId() != null ? a.getDetectionLogicId().toString() : null,
        a.getCanSignalId() != null ? a.getCanSignalId().toString() : null,
        a.getAnomalyType(),
        a.getSignalName(),
        a.getSymptom(),
        a.getCause(),
        a.getCountermeasure(),
        a.isHasSolution(),
        fromJson(a.getSolutionSteps()),
        fromJson(a.getPreventionMeasures()),
        a.getAverageRating(),
        a.getRatingCount());
  }

  private KnowledgeArticleCommentDto toCommentDto(KnowledgeArticleComment c) {
    return new KnowledgeArticleCommentDto(
        c.getId().toString(),
        c.getArticleId().toString(),
        c.getAuthorUserId() != null ? c.getAuthorUserId().toString() : null,
        c.getAuthorName(),
        c.getContent(),
        c.getRating(),
        c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
  }

  private List<String> fromJson(String json) {
    if (json == null || json.isBlank()) return List.of();
    try { return objectMapper.readValue(json, new TypeReference<List<String>>() {}); }
    catch (Exception e) { return List.of(); }
  }

  private String toJson(List<String> list) {
    if (list == null || list.isEmpty()) return "[]";
    try { return objectMapper.writeValueAsString(list); }
    catch (Exception e) { return "[]"; }
  }

  private static String s(String v) { return v != null ? v : ""; }
}
