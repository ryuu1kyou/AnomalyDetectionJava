---
name: anomaly-dr-backlog
description: Known technical debt and DR backlog for AnomalyDetectionJava — list of deviations from spec that are acknowledged but not yet fixed. Use when planning next work or assessing what's missing before adding new features.
---

# DR バックログ (既知の技術的負債)

最終更新: 2026-04-28

## 優先度: High (次フェーズで対処)

### M3 横断機能 (全て未着手)
- `permissions` — permission_grants テーブル + PermissionDefinitionContributor + DB 管理
- `settings` — settings テーブル (Global/Tenant/User) + Caffeine キャッシュ
- `features` — Togglz + feature_values テーブル
- `auditlogging` — AOP インターセプタ + audit_logs + Hibernate Envers (AuditLogAspect は基本実装済・HTTP ステータス捕捉修正済)
- `backgroundjobs` — Quartz + ShedLock (POM にバージョンのみ定義済み)
- `blobstoring` — Spring Content JPA
- `localization` — MessageSource + LocaleResolver + ja/en 切替 (ドメインメッセージキー追加済、LocaleResolver 未実装)

### フロントエンド E2E テスト (未着手)
- Playwright E2E → 未追加
- `frontend/tests/e2e/` ディレクトリ未作成

## 優先度: Medium

### コード品質ツール未導入
- Spotless + Google Java Format (pom.xml に宣言済み・設定検証未完)
- Checkstyle
- SpotBugs + FindSecBugs (pom.xml に宣言済み・設定検証未完)
- Jacoco (pom.xml に宣言済み・設定検証未完。目標: Domain ≥ 90%, Application ≥ 80%)
- OWASP Dependency-Check
- Maven Enforcer
- Prettier (フロントエンド)

### ArchUnit ルール不足
現在の 4 ルールに追加が必要:
- JPA エンティティは `infrastructure` のみ (現在 domain に @Entity)
- DTO は `application-contracts` のみ
- `internal` パッケージの外部参照禁止

## 優先度: Low

### In-memory ページング
- `TenantAppService.getList()`, `UserAppService.getList()` 等が `findAll()` 後 Java フィルタ
- 大量データ時のパフォーマンス問題
- M3 完了後に JPA `Pageable` ベースに置換

### Projects ページ実 API 未接続
- `ProjectsPage` → mock データ、実 API 未接続
- M3 完了後に Projects Domain Aggregate + JPA Repository 実装後に置換

### JMH パフォーマンステスト
- `host/src/jmh/java/` ディレクトリ未作成

## 修正済み (このフェーズで対応)

- [x] `@SQLDelete` 全 17 エンティティへ追加
- [x] `@PreAuthorize` 全 10 AppService へ追加
- [x] `getRecommendations()` ロジックバグ修正 (`Map.entry` を使用)
- [x] `testConnection()` の `@PreAuthorize` 漏れ修正
- [x] テスト JWT への authorities 追加 (68/68 PASS)
- [x] `AuditLogAspect` HTTP ステータスコード捕捉修正 (`HttpServletResponse.getStatus()`)
- [x] db-migrator 全ドメイン権限シード (~60 permissions、CanSystemCategory 8件、DetectionTemplate 4件)
- [x] i18n メッセージキー拡充 (ja/en、全 10 ドメインモジュール対応)
- [x] バックエンドテストモジュール分散配置 (domain: 19テスト、application: 12テスト、host: 41テスト = 計72)
- [x] Vitest + React Testing Library + MSW フロントエンドテスト基盤構築
- [x] MapStruct `@Mapper` 導入 (CanSignalMapper・CanAnomalyDetectionLogicMapper)
- [x] Safety / KnowledgeBase / OemTraceability / CanSignalSpec 完全 CRUD フロントエンドページ実装
