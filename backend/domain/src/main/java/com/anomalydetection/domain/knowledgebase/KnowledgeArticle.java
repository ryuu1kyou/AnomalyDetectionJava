package com.anomalydetection.domain.knowledgebase;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "knowledge_articles")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLDelete(sql = "UPDATE knowledge_articles SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class KnowledgeArticle extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(nullable = false, length = 500)
  private String title;

  @Column(nullable = false, columnDefinition = "LONGTEXT")
  private String content;

  @Column(length = 2000)
  private String summary;

  @Enumerated(EnumType.STRING)
  @Column(length = 32)
  private KnowledgeCategory category;

  @Column(columnDefinition = "LONGTEXT")
  private String tags;

  @Column(name = "view_count", nullable = false)
  private int viewCount;

  @Column(name = "useful_count", nullable = false)
  private int usefulCount;

  @Column(name = "is_published", nullable = false)
  private boolean isPublished;

  @Column(name = "published_at")
  private Instant publishedAt;

  // Domain context
  @Column(name = "related_anomaly_id", columnDefinition = "BINARY(16)")
  private UUID relatedAnomalyId;

  @Column(name = "detection_logic_id", columnDefinition = "BINARY(16)")
  private UUID detectionLogicId;

  @Column(name = "can_signal_id", columnDefinition = "BINARY(16)")
  private UUID canSignalId;

  @Column(name = "anomaly_type", length = 32)
  private String anomalyType;

  @Column(name = "signal_name", length = 200)
  private String signalName;

  // Structured knowledge
  @Column(length = 2000)
  private String symptom;

  @Column(length = 2000)
  private String cause;

  @Column(length = 2000)
  private String countermeasure;

  @Column(name = "has_solution", nullable = false)
  private boolean hasSolution;

  @Column(name = "solution_steps", columnDefinition = "LONGTEXT")
  private String solutionSteps;

  @Column(name = "prevention_measures", columnDefinition = "LONGTEXT")
  private String preventionMeasures;

  // Ratings
  @Column(name = "average_rating", nullable = false)
  private double averageRating;

  @Column(name = "rating_count", nullable = false)
  private int ratingCount;

  protected KnowledgeArticle() {}

  public KnowledgeArticle(UUID id, String title, String content) {
    this.id = id;
    this.title = title;
    this.content = content;
    this.isPublished = false;
    this.viewCount = 0;
    this.usefulCount = 0;
    this.averageRating = 0.0;
    this.ratingCount = 0;
    this.hasSolution = false;
  }

  public void publish() {
    this.isPublished = true;
    this.publishedAt = Instant.now();
  }

  public void unpublish() {
    this.isPublished = false;
  }

  public void incrementViewCount() {
    this.viewCount++;
  }

  public void incrementUsefulCount() {
    this.usefulCount++;
  }

  public void applyRating(int rating) {
    double total = this.averageRating * this.ratingCount + rating;
    this.ratingCount++;
    this.averageRating = total / this.ratingCount;
  }

  @Override
  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }

  public String getSummary() { return summary; }
  public void setSummary(String summary) { this.summary = summary; }

  public KnowledgeCategory getCategory() { return category; }
  public void setCategory(KnowledgeCategory category) { this.category = category; }

  public String getTags() { return tags; }
  public void setTags(String tags) { this.tags = tags; }

  public int getViewCount() { return viewCount; }
  public int getUsefulCount() { return usefulCount; }
  public boolean isPublished() { return isPublished; }
  public Instant getPublishedAt() { return publishedAt; }

  public UUID getRelatedAnomalyId() { return relatedAnomalyId; }
  public void setRelatedAnomalyId(UUID relatedAnomalyId) { this.relatedAnomalyId = relatedAnomalyId; }

  public UUID getDetectionLogicId() { return detectionLogicId; }
  public void setDetectionLogicId(UUID detectionLogicId) { this.detectionLogicId = detectionLogicId; }

  public UUID getCanSignalId() { return canSignalId; }
  public void setCanSignalId(UUID canSignalId) { this.canSignalId = canSignalId; }

  public String getAnomalyType() { return anomalyType; }
  public void setAnomalyType(String anomalyType) { this.anomalyType = anomalyType; }

  public String getSignalName() { return signalName; }
  public void setSignalName(String signalName) { this.signalName = signalName; }

  public String getSymptom() { return symptom; }
  public void setSymptom(String symptom) { this.symptom = symptom; }

  public String getCause() { return cause; }
  public void setCause(String cause) { this.cause = cause; }

  public String getCountermeasure() { return countermeasure; }
  public void setCountermeasure(String countermeasure) { this.countermeasure = countermeasure; }

  public boolean isHasSolution() { return hasSolution; }
  public void setHasSolution(boolean hasSolution) { this.hasSolution = hasSolution; }

  public String getSolutionSteps() { return solutionSteps; }
  public void setSolutionSteps(String solutionSteps) { this.solutionSteps = solutionSteps; }

  public String getPreventionMeasures() { return preventionMeasures; }
  public void setPreventionMeasures(String preventionMeasures) { this.preventionMeasures = preventionMeasures; }

  public double getAverageRating() { return averageRating; }
  public int getRatingCount() { return ratingCount; }
}
