---
name: anomaly-domain
description: Domain layer patterns for AnomalyDetectionJava — Entity base classes, soft delete (@SQLDelete + @SQLRestriction), multi-tenancy (@Filter), FullAuditedEntity, AggregateRoot. Use when creating or reviewing domain entities, repositories, or domain services.
---

# Domain Layer Patterns

## Base Class Hierarchy

```
FullAuditedEntity<UUID>          ← 監査 + ソフト削除が必要なエンティティ
  └── (+ MultiTenant interface)  ← テナント分離も必要な場合
AggregateRoot<UUID>              ← ドメインイベントを発行する集約ルート
```

## 必須アノテーションセット (FullAuditedEntity を継承する場合)

`@SQLDelete` と `@SQLRestriction` は **必ずペアで** 付与する。どちらか片方だけでは動作しない。

```java
@Entity
@Table(name = "can_signals")
@SQLDelete(sql = "UPDATE can_signals SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
// マルチテナントの場合は以下も追加
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId OR tenant_id IS NULL")
public class CanSignal extends FullAuditedEntity<UUID> implements MultiTenant {
    // ...
}
```

## テーブル名命名規則

| エンティティ | テーブル名 |
|---|---|
| CanSignal | can_signals |
| CanAnomalyDetectionLogic | can_anomaly_detection_logics |
| DetectionTemplate | detection_templates |
| KnowledgeArticle | knowledge_articles |
| OemApproval | oem_approvals |

ルール: `snake_case` 複数形。

## コンストラクタパターン

```java
// ID は外から渡す (UUID.randomUUID() は AppService で生成)
public CanSignal(UUID id, UUID tenantId, String signalIdentifier, String name) {
    super(id);
    this.tenantId = tenantId;
    this.signalIdentifier = Objects.requireNonNull(signalIdentifier);
    this.name = Objects.requireNonNull(name);
}

// JPA 用 protected コンストラクタ (必須)
protected CanSignal() {}
```

## Repository パターン

- インターフェースは `domain` モジュールに定義
- 実装は `infrastructure` モジュールに置く
- Spring Data JPA の命名規約で充足できる場合は実装クラス不要

```java
// domain モジュール
public interface CanSignalRepository extends JpaRepository<CanSignal, UUID> {
    boolean existsBySignalIdentifier(String signalIdentifier);
    List<CanSignal> findAllByStatus(SignalStatus status);
}
```

## ソフト削除の確認ポイント

新規エンティティ追加時のチェックリスト:
- [ ] `@SQLDelete` が設定されている
- [ ] `@SQLRestriction("is_deleted = false")` が設定されている
- [ ] テーブル名が `@Table(name = "...")` と一致している
- [ ] MultiTenant が必要なら `@Filter` も設定
- [ ] Liquibase changelog に `is_deleted`, `deleted_at`, `deleted_by` カラムがある

## アンチパターン

```java
// ❌ @SQLDelete だけで @SQLRestriction がない → 論理削除したデータが検索に出る
@SQLDelete(sql = "UPDATE ... SET is_deleted = true ...")
// @SQLRestriction が無い!

// ❌ deleteById() を直接呼ぶ → @SQLDelete があれば UPDATE になるので OK だが、
//    Repository 経由にしない直接 DELETE クエリは論理削除をバイパスする

// ❌ domain エンティティで @Autowired を使う → ArchUnit テストで失格
@Autowired
private SomeService service; // NG: domain は Spring に依存してはならない
```
