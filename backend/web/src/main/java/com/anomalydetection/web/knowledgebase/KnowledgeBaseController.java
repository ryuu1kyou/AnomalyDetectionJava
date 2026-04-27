package com.anomalydetection.web.knowledgebase;

import com.anomalydetection.application.knowledgebase.KnowledgeBaseAppService;
import com.anomalydetection.contracts.knowledgebase.CreateKnowledgeArticleCommentDto;
import com.anomalydetection.contracts.knowledgebase.CreateKnowledgeArticleDto;
import com.anomalydetection.contracts.knowledgebase.GetKnowledgeArticlesInput;
import com.anomalydetection.contracts.knowledgebase.KnowledgeArticleCommentDto;
import com.anomalydetection.contracts.knowledgebase.KnowledgeArticleDto;
import com.anomalydetection.contracts.knowledgebase.UpdateKnowledgeArticleDto;
import com.anomalydetection.domain.knowledgebase.KnowledgeCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/knowledge-articles")
public class KnowledgeBaseController {

  private final KnowledgeBaseAppService appService;

  public KnowledgeBaseController(KnowledgeBaseAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public List<KnowledgeArticleDto> getList(
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) KnowledgeCategory category,
      @RequestParam(required = false) Boolean isPublished,
      @RequestParam(required = false) String detectionLogicId,
      @RequestParam(required = false) String canSignalId,
      @RequestParam(required = false) Integer skipCount,
      @RequestParam(required = false) Integer maxResultCount) {
    return appService.getList(new GetKnowledgeArticlesInput(
        filter, category, isPublished, detectionLogicId, canSignalId,
        skipCount, maxResultCount));
  }

  @GetMapping("/{id}")
  public ResponseEntity<KnowledgeArticleDto> get(@PathVariable UUID id) {
    return appService.getById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public KnowledgeArticleDto create(@RequestBody CreateKnowledgeArticleDto input) {
    return appService.create(input);
  }

  @PutMapping("/{id}")
  public ResponseEntity<KnowledgeArticleDto> update(
      @PathVariable UUID id, @RequestBody UpdateKnowledgeArticleDto input) {
    return appService.update(id, input).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return appService.delete(id) ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }

  @PostMapping("/{id}/publish")
  public ResponseEntity<KnowledgeArticleDto> publish(@PathVariable UUID id) {
    return appService.publish(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/unpublish")
  public ResponseEntity<KnowledgeArticleDto> unpublish(@PathVariable UUID id) {
    return appService.unpublish(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/mark-useful")
  public ResponseEntity<KnowledgeArticleDto> markAsUseful(@PathVariable UUID id) {
    return appService.markAsUseful(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/rate")
  public ResponseEntity<KnowledgeArticleDto> rate(
      @PathVariable UUID id, @RequestParam int rating) {
    return appService.rate(id, rating).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/popular")
  public List<KnowledgeArticleDto> getPopular(
      @RequestParam(required = false, defaultValue = "10") int limit) {
    return appService.getPopular(limit);
  }

  @GetMapping("/recommendations")
  public List<KnowledgeArticleDto> getRecommendations(
      @RequestParam(required = false) String detectionLogicId,
      @RequestParam(required = false) String canSignalId,
      @RequestParam(required = false) String anomalyType,
      @RequestParam(required = false, defaultValue = "10") int limit) {
    return appService.getRecommendations(detectionLogicId, canSignalId, anomalyType, limit);
  }

  @GetMapping("/{id}/comments")
  public List<KnowledgeArticleCommentDto> getComments(@PathVariable UUID id) {
    return appService.getComments(id);
  }

  @PostMapping("/comments")
  public KnowledgeArticleCommentDto addComment(
      @RequestBody CreateKnowledgeArticleCommentDto input) {
    return appService.addComment(input);
  }

  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
    return appService.deleteComment(commentId) ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}
