# AnomalyDetectionJava — 開発歴史・マイルストーン記録

このファイルは **M0〜M9 の実装遷移** と **主要な設計決定の記録** をまとめます。

---

## マイルストーン一覧

| # | 完了日 | 内容 | タグ |
|---|--------|------|------|
| M0 | 2026-04-25 | 基盤セットアップ | `m0-baseline` |
| M1 | 2026-04-27 | Identity + Multi-Tenancy | `m1-identity-multitenancy` |
| M2 | 2026-04-27 | Spring Authorization Server | `m2-spring-authorization-server` |
| M3 | 2026-04-28 | 横断機能全整備 | — |
| M4 | 2026-04-29 | コアドメイン 10 モジュール | — |
| M5 | 2026-04-29 | フロントエンド基盤 | — |
| M6 | 2026-04-30 | フロントエンド全機能ページ | — |
| M7 | 2026-05-01 | テスト整備 (87 件) | — |
| M8 | 2026-05-02 | トレーサビリティ強化 + CI/CD | — |
| M9 | 2026-05-04 | Decision Ledger / V&V / CrossOEM レポート | — |

---

## M0 — 基盤セットアップ (2026-04-25)

**目的**: Java マルチモジュール雛形を動かす最小構成

### 実装内容

- Maven マルチモジュール 9 モジュール作成 (domain-shared / domain / application-contracts / application / infrastructure / web / auth-server / host / db-migrator)
- 親 POM: Spring Boot 3.3 BOM、Java 21、MapStruct、Lombok、Spring Modulith BOM
- `host` で空 Spring Boot アプリ起動 (`/actuator/health` 返却)
- `infrastructure` で MySQL 8 接続 + Liquibase で空スキーマ初期化 (001-010 changeset)
- ArchUnit テストでモジュール境界検証

### 成果物

- 空アプリ起動確認
- Liquibase 自動マイグレーション動作
- ArchUnit 緑

---

## M1 — Identity + Multi-Tenancy (2026-04-27)

**目的**: テナント管理とユーザー/ロール管理の基盤

### 実装内容

- `multitenancy` モジュール: `tenants` テーブル + Tenant Aggregate + `TenantResolutionFilter` + Hibernate `@Filter` 基盤
- `identity` モジュール: User / Role / OrganizationUnit エンティティ + CRUD
- `db-migrator` から **デフォルトテナント / admin ユーザー / admin ロール** 初期投入
- `web` に Tenant/User CRUD REST API 公開

### 成果物

- テナント・ユーザー CRUD 動作
- `tenant_id` フィルタ機能確認
- ArchUnit 緑 + 統合テスト追加

---

## M2 — Spring Authorization Server (2026-04-27)

**目的**: OAuth2/OIDC 認可サーバ + フロントの PKCE ログイン

### 実装内容

- `auth-server` モジュールに Spring Authorization Server 組み込み
- `oauth2_*` JDBC スキーマを Liquibase で投入
- `db-migrator` で `anomaly-detection-spa` クライアント投入
- `JwtTokenCustomizer` で `permissions` / `roles` / `tenantid` クレーム付与
- 最小 Thymeleaf ログイン画面
- リソースサーバ側 JWT 検証設定 (`SecurityConfiguration.jwtAuthConverter()`)
- `react-oidc-context` で PKCE 認可コードフロー実装

### 成果物

- PKCE フローで JWT 取得確認
- Bearer JWT で保護 API を呼び出し確認

---

## M3 — 横断機能モジュール (2026-04-28)

**目的**: 全横断機能 (ABP の Cross-cutting Concerns 相当) を整備

### 実装内容

- **権限管理**: `PermissionDefinitionContributor` パターン / `permission_grants` テーブル / `@PreAuthorize` 検証
- **設定管理**: `settings` テーブル + Caffeine キャッシュ + REST API
- **機能フラグ**: Togglz + `feature_values` テーブル + REST API
- **監査ログ**: Spring AOP インターセプタ + Hibernate Envers + `audit_logs` テーブル + REST API
- **バックグラウンドジョブ**: Quartz Scheduler + ShedLock
- **BLOB ストレージ**: Spring Content JPA
- **i18n**: Spring `MessageSource` + `LocaleResolver`
- フロントエンド管理ページ: 監査ログ / 設定 / 機能フラグ / 権限管理 (4 ページ)

### 成果物

- 85 テスト全 PASS (当時)
- 横断機能バックエンド REST API 完成
- フロント管理ページ 4 画面

---

## M4 — コアドメイン移植 (2026-04-29)

**目的**: ドメイン 10 モジュールの REST API を完成

### 実装順序 (依存関係順)

1. `cansspecification` (CAN 信号仕様)
2. `cansignals` (CAN 信号)
3. `detectiontemplates` (検出テンプレート)
4. `anomalydetection` (ロジック + 結果)
5. `projects` (プロジェクト)
6. `safety` (ISO 26262)
7. `oemtraceability` (OEM 承認・カスタマイズ)
8. `knowledgebase` (ナレッジベース)
9. `similarpatternsearch` (類似パターン検索)
10. `integration` (インテグレーション)

### 各モジュールの実装内容

各モジュールで以下を実装:
- JPA エンティティ + Liquibase changelog
- Repository + Domain Service
- AppService + DTO + MapStruct Mapper + 権限定数クラス
- REST Controller + OpenAPI スキーマ
- 統合テスト

### 成果物

- 全 10 ドメインの REST API 完成
- Swagger UI で全エンドポイント確認可能

---

## M5 — フロントエンド基盤 (2026-04-29)

**目的**: React フロントエンドの認証・状態管理基盤

### 実装内容

- Vite + TypeScript strict + React 19 雛形
- Ant Design 6.x + テーマ設定
- `react-oidc-context` で PKCE 認可コードフロー
- TanStack Query 5 + Zustand 5
- `openapi-fetch` HTTP クライアント + `apiFetch` ラッパー
- 共通レイアウト: Sider + Header + Content (`RootLayout`)
- 認可ガードルート (`AuthGuard`)
- `/callback` コールバックページ
- `usePermissions` hook + `RequirePermission` コンポーネント

### 成果物

- ログイン → ダッシュボード → API 接続の基本フロー動作

---

## M6 — フロントエンド全機能ページ (2026-04-30)

**目的**: 全ドメインの CRUD 画面を React で実装

### 実装内容

各機能ページ (Ant Design `Table` + `Modal` / `Drawer` + `Form` + `Popconfirm`):

- CAN 信号一覧・CRUD (`CanSignalListPage`)
- CAN 信号仕様 (`CanSignalSpecPage`)
- 検出テンプレート (`DetectionTemplateListPage`)
- 異常検出ロジック (`AnomalyDetectionListPage`)
- プロジェクト管理 (`ProjectsPage` / `ProjectListPage` / `ProjectDetailPage`)
- Safety ISO 26262 (`SafetyPage` — トレーサビリティ詳細 Drawer 付き)
- ナレッジベース (`KnowledgeBasePage`)
- OEM トレーサビリティ (`OemTraceabilityPage`)
- 類似パターン検索 (`SimilarPatternSearchPage`)
- インテグレーション (`IntegrationListPage`)

### 技術パターン

- TanStack Query + `apiFetch` で全ページ統一
- `RequirePermission` で全ルートを権限ガード
- `RootLayout.filterMenu()` で権限に応じてメニュー表示制御

### 成果物

- 全ドメインの基本 CRUD 画面完成
- 権限ガードが機能する管理画面

---

## M7 — テスト整備 (2026-05-01)

**目的**: 統合テストカバレッジを体系化

### 実装内容

**ドメインユニットテスト (19 件):**
- `CanSignalTest`
- `AnomalyDetectionLogicStatusTransitionTest`
- `SimilarPatternSearchServiceTest`

**アプリサービス単体テスト (12 件):**
- `CanSignalAppServiceTest`
- `AnomalyDetectionLogicAppServiceTest`

**API 統合テスト (60 件):**
- `CanSignalApiTest`
- `DetectionTemplateApiTest`
- `AnomalyDetectionLogicApiTest`
- `SoftDeleteApiTest`
- `ProjectsApiTest` (6 テスト)
- その他ドメイン API テスト

**その他:**
- MapStruct `@Mapper` の本番導入
- テストモジュール分散配置 (`host/src/test/` に統合テスト集約)
- Vitest + RTL + MSW フロントエンドテスト基盤
- AuditLog HTTP ステータス修正
- 全ドメイン権限シード整備

### 成果物

- **87 件全 PASS** (2026-05-01 時点)

---

## M8 — トレーサビリティ強化 + CI/CD (2026-05-02)

**目的**: ISO 26262 トレーサビリティ機能の強化と CI/CD 整備

### Phase A — 状態遷移ガード + トレーサビリティキー (A-1〜A-5)

- `SafetyTraceRecord`, `OemApproval`, `OemCustomization` に状態遷移ガード
  - `submit()` / `startReview()` / `approve()` / `reject()` の不正遷移を `IllegalStateException` でガード
  - API 層で `@RestControllerAdvice` が 409 Conflict に変換
- Liquibase changeset 023: トレーサビリティキー列追加
  - `feature_id`, `decision_id`, `change_id`, `if_impact`, `unknown_until`, `unknown_owner_id`
  - `design_rationale`, `assumption`, `constraint_text`, `doc_sync_status`, `scope`, `applicability`
- 統合テスト 8 件追加 (SafetyTrace / OemApproval / OemCustomization 状態遷移)

### Phase B — feature_id 横断検索 API

- `GET /api/app/traceability/feature/{featureId}`
  - Safety レコードと OEM 承認を `feature_id` で横断突合
  - 権限: `SafetyTrace.Records.Default` + `OemTraceability.Approvals.Default`
- 統合テスト 2 件追加 (`FeatureTraceabilityApiTest`)

### Phase C — フロントエンド権限 UI

- C-1: `usePermissions` hook + `RequirePermission` + メニュー権限フィルタ + ルート保護
- C-2: Safety 詳細 Drawer (トレーサビリティキー全表示) + OEM 突合 Drawer + トレサビバッジ

### Phase D — GitHub Actions CI/CD

- `backend-ci.yml`: `./mvnw -B verify` (全テスト + 静的解析)
- `frontend-ci.yml`: ESLint + Vitest + Vite build
- `codeql.yml`: セキュリティ解析
- `dependabot.yml`: Maven + npm 依存更新自動 PR
- `.github/PULL_REQUEST_TEMPLATE.md`: トレーサビリティチェックリスト付き PR テンプレート

---

## M9 — Decision Ledger / V&V / CrossOEM レポート (2026-05-04)

**目的**: ISO 26262 の上位仕様に対応した台帳機能の追加

### Phase A — Decision Ledger (M9-A)

**バックエンド:**
- `DecisionLedger` エンティティ (domain)
- `DecisionLedgerPermissions` 権限定数 (application-contracts)
- `DecisionLedgerAppService` (application)
- `DecisionLedgerController` (`/api/app/decision-ledger`)
- Liquibase changeset 025 (`decision_ledgers` テーブル)
- Liquibase changeset 026 (`safety_trace_records` 拡張: `svn_rev`, `module_id`, `if_version`, `change_type`)
- `SafetyPermissionDefinitionContributor` に DecisionLedger 権限を追加
- `LocalDevSeeder` + `SeedDataInitializer` に DecisionLedger 権限を追加

**テスト:**
- `DecisionLedgerApiTest` (5 件)

### Phase B — V&V 記録 (M9-B)

- Safety レコードに対して検証 (Verification) / 妥当性確認 (Validation) を追加
- `SafetyTraceController` に `/verifications` / `/validations` サブリソース
- テスト: `SafetyTraceVandVApiTest` (4 件)

### Phase C — CrossOEM レポート (M9-C)

- `OemTraceabilityReportAppService` + `OemTraceabilityReportController`
- `GET /api/app/oem-traceability-report/by-feature?featureId=xxx`
  - feature_id 別に OEM ごとの承認状況をサマリー
  - `OemTraceabilityReportDto` (totalOems / approvedCount / pendingCount / byOem[])
- テスト: `OemTraceabilityReportApiTest` (3 件)

### 権限シード修正

- M9-A 後に `ClassNotFoundException: PermissionManager` が発生
- 原因: `DecisionLedgerPermissions` (新クラス) を `LocalDevSeeder` が参照するが、`application-contracts` の旧 JAR がキャッシュされていた
- 修正: `./mvnw clean install -DskipTests` で全モジュール再ビルド・再インストール

### SafetyPage.tsx フィールド名修正

- `SafetyTraceRecord.status` → `approvalStatus` に修正 (バックエンドの DTO と一致させる)

---

## 設計決定ログ

### ADR-001: テストに MariaDB4j を採用 (M0)

**決定**: Testcontainers の代わりに MariaDB4j (組み込み MariaDB) を採用

**理由**:
- Docker 不使用環境 (Windows 11 ホスト直接実行) のため Testcontainers が使えない
- MariaDB4j は JVM 内で MariaDB を起動し、Docker ゼロで本番相当の DB テストが可能

**影響**: `MariaDB4jExtension` を全統合テストで共有

---

### ADR-002: 権限を JWT に埋め込む方式 (M2)

**決定**: リソースサーバが毎回 DB を参照せず、JWT の `permissions` クレームから権限を取得

**理由**:
- ABP の `oi_prst` クレーム方式と同等の設計
- DB アクセスを JWT 発行時のみに限定しパフォーマンスを向上

**影響**: 権限変更は次回ログイン時まで反映されない (トレードオフとして許容)

---

### ADR-003: LocalDevSeeder が全権限をシード (M8)

**決定**: `LocalDevSeeder` (`@Profile("local")`) が全ドメイン権限を admin ロールに付与

**理由**:
- `SeedDataInitializer` (db-migrator) は `host` クラスパスに含まれないため、ローカル起動時には実行されない
- `LocalDevSeeder` が identity 権限しかシードしていなかったため、全ドメインページが blank になっていた (M8 前)

**影響**:
- 新しい権限定数を追加する際は必ず `LocalDevSeeder` と `SeedDataInitializer` の両方を更新
- 権限追加後はバックエンド再起動 + ログアウト→再ログインが必要

---

### ADR-004: フロントの型は手書き + openapi-fetch (M5)

**決定**: OpenAPI 自動生成クライアントの代わりに `openapi-fetch` + 手書き interface を使用

**理由**:
- バックエンド起動なしで開発を並行進行するため
- `npm run api:generate` は バックエンド起動中に手動実行する運用

**影響**:
- 型が古くなるリスクがある
- 将来的に `api:generate` で `schema.d.ts` を生成し、手書き型を置換する予定

---

### ADR-005: SafetyTraceRecord の承認ステータスフィールド名 (M6)

**決定**: `SafetyTraceRecord` の承認状態フィールドは `approvalStatus` とする

**理由**:
- `status` という汎用名だと他ドメイン (`OemApproval.status`, `AnomalyDetectionLogic.status`) と混同しやすい
- Safety の承認フローが特殊 (DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED/REJECTED) であることを明示

**影響**:
- フロントエンド `SafetyPage.tsx` では `approvalStatus` を使用 (`status` にするとバグになる)
