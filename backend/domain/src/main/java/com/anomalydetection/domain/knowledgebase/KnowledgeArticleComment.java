package com.anomalydetection.domain.knowledgebase;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "knowledge_article_comments")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLRestriction("is_deleted = false")
public class KnowledgeArticleComment extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "article_id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID articleId;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "author_user_id", columnDefinition = "BINARY(16)")
  private UUID authorUserId;

  @Column(name = "author_name", length = 200)
  private String authorName;

  @Column(nullable = false, length = 2000)
  private String content;

  @Column(nullable = false)
  private int rating;

  protected KnowledgeArticleComment() {}

  public KnowledgeArticleComment(UUID id, UUID articleId, String content) {
    this.id = id;
    this.articleId = articleId;
    this.content = content;
    this.rating = 0;
  }

  public void updateContent(String content) { this.content = content; }
  public void updateRating(int rating) { this.rating = Math.max(0, Math.min(5, rating)); }

  @Override
  public UUID getId() { return id; }

  public UUID getArticleId() { return articleId; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public UUID getAuthorUserId() { return authorUserId; }
  public void setAuthorUserId(UUID authorUserId) { this.authorUserId = authorUserId; }

  public String getAuthorName() { return authorName; }
  public void setAuthorName(String authorName) { this.authorName = authorName; }

  public String getContent() { return content; }
  public int getRating() { return rating; }
}
