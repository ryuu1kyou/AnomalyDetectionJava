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
- `auditlogging` — AOP インターセプタ + audit_logs + Hibernate Envers
- `backgroundjobs` — Quartz + ShedLock (POM にバージョンのみ定義済み)
- `blobstoring` — Spring Content JPA
- `localization` — MessageSource + LocaleResolver + ja/en 切替

### フロントエンドテスト (完全欠落)
- Vitest + React Testing Library + MSW → `package.json` 未追加
- Playwright E2E → 未追加
- `frontend/tests/e2e/` ディレクトリ未作成

## 優先度: Medium

### バックエンドテストモジュール配置
- 現状: 全テストが `host/src/test/java/` に集中
- 仕様: `domain/`, `application/`, `infrastructure/` に分散配置
- ドメイン単体テストが Spring コンテキスト起動コストを負担している

### コード品質ツール未導入
- Spotless + Google Java Format
- Checkstyle
- SpotBugs + FindSecBugs
- Jacoco (Domain ≥ 90%, Application ≥ 80%)
- OWASP Dependency-Check
- Maven Enforcer
- Prettier (フロントエンド)

### ArchUnit ルール不足
現在の 4 ルールに追加が必要:
- JPA エンティティは `infrastructure` のみ (現在 domain に @Entity)
- DTO は `application-contracts` のみ
- `internal` パッケージの外部参照禁止

### MapStruct 未使用
- POM で宣言済みだが全 AppService が手書き `toDto()` を使用
- MapStruct `@Mapper` / `@Mapping` は未使用

## 優先度: Low

### In-memory ページング
- `TenantAppService.getList()`, `UserAppService.getList()` 等が `findAll()` 後 Java フィルタ
- 大量データ時のパフォーマンス問題
- M3 完了後に JPA `Pageable` ベースに置換

### db-migrator シード不足
- `@PreAuthorize` に対応する全ドメイン権限が admin ロールに付与されていない
- CanSystemCategory マスター未投入
- 既定 DetectionTemplate 未投入

### フロントエンドスタブページ
- `SafetyPage`, `OemTraceabilityPage`, `KnowledgeBasePage`, `CanSignalSpecPage` → 一覧スタブのみ
- Projects → mock データ、実 API 未接続

### JMH パフォーマンステスト
- `host/src/jmh/java/` ディレクトリ未作成

## 修正済み (このフェーズで対応)

- [x] `@SQLDelete` 全 17 エンティティへ追加
- [x] `@PreAuthorize` 全 10 AppService へ追加
- [x] `getRecommendations()` ロジックバグ修正 (`Map.entry` を使用)
- [x] `testConnection()` の `@PreAuthorize` 漏れ修正
- [x] テスト JWT への authorities 追加 (68/68 PASS)
