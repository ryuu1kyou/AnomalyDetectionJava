# AnomalyDetectionJava — プロジェクトメモ (CLAUDE.md)

このリポジトリは、`.NET 10 + ABP vNext 9.3.5` で実装された CAN 異常検出管理システム
([../AnomalyDetection](../AnomalyDetection)) を **Java / Spring Boot + React** に
**完全機能パリティで移植**するためのリポジトリです。
既存の .NET 版とは独立した GitHub リポジトリとして運用される想定であり、
`.NET 版のソースには影響を与えません`。

> 本ファイルは、ブレインストーミング段階で合意した設計前提を記録したものです。
> 詳細な設計書 (spec) は [docs/superpowers/specs/](docs/superpowers/specs/) に配置します。
> 実装計画 (plan) は spec 承認後に作成されます。

---

## 1. プロジェクト目的

- **完全機能パリティ移植**: 既存の .NET / ABP 版と同等のドメインと機能をすべて Java 側で再現する
  - 主要ドメイン: `CanSignal`, `CanAnomalyDetectionLogic`, `AnomalyDetectionResult`,
    `CanSpecification`, `DetectionTemplate`, `Project`, `VehiclePhase`,
    `Safety (ISO 26262)`, `KnowledgeBase`, `OemTraceability`, `SimilarPatternSearch`,
    `Integration` ほか
  - 横断機能: マルチテナント、認証/認可、権限管理、監査ログ、設定管理、機能管理、
    バックグラウンドジョブ、ローカライズ、BLOB ストレージ、ドメインイベント
- 自動車業界 (OEM 複数社) のマルチテナント Web アプリ
- ISO 26262 機能安全のトレーサビリティを維持

## 2. 技術スタック (合意済み)

### 2.1 バックエンド

| 領域 | 採用技術 | ABP 対応 |
| --- | --- | --- |
| 言語 / ランタイム | Java 21 (LTS) | .NET 10 |
| ビルドツール | **Maven** マルチモジュール | (.NET) `*.sln` + `*.csproj` |
| Web / モジュール | Spring Boot 3.3 + **Spring Modulith** | ABP モジュール |
| 認可サーバ | **Spring Authorization Server** (組み込み) | OpenIddict |
| リソースサーバ | Spring Security OAuth2 Resource Server | Volo.Abp.AspNetCore.Authentication |
| ORM | Spring Data JPA + Hibernate 6 | Entity Framework Core |
| DB マイグレーション | **Liquibase** | EF Core Migrations |
| マッピング | MapStruct | AutoMapper |
| 監査 | Spring Data JPA Auditing + Hibernate Envers | `IAuditedObject` / `AuditLog` |
| マルチテナント | Hibernate `@Filter` + AOP で `TenantId` 自動注入 | `IMultiTenant` |
| イベント | Spring Modulith Application Events | Volo.Abp.EventBus |
| バックグラウンドジョブ | Quartz Scheduler + ShedLock | Volo.Abp.BackgroundJobs |
| 機能管理 | Togglz + 自前 `Feature` テーブル | Volo.Abp.FeatureManagement |
| 設定管理 | 自前 `Setting` テーブル + キャッシュ | Volo.Abp.SettingManagement |
| キャッシュ | Spring Cache + Caffeine (将来 Redis 対応) | Volo.Abp.Caching |
| BLOB ストレージ | Spring Content (DB BLOB / 将来 S3) | Volo.Abp.BlobStoring.Database |
| i18n | Spring `MessageSource` + `LocaleResolver` | Volo.Abp.Localization |
| WebSocket | Spring WebSocket + STOMP | SignalR |
| API ドキュメント | springdoc-openapi (OpenAPI 3) | ABP Swagger |
| テスト | JUnit 5 + **MariaDB4j** (組み込み MariaDB / Docker 不要) + ArchUnit | xUnit + ABP TestBase |
| ロギング | SLF4J + Logback + Logstash JSON | Serilog |

### 2.2 フロントエンド

| 領域 | 採用技術 |
| --- | --- |
| ビルド | Vite |
| 言語 | TypeScript |
| フレームワーク | React 18+ |
| UI ライブラリ | **Ant Design** |
| 状態管理 | Zustand |
| サーバ状態 / データフェッチ | TanStack Query |
| HTTP クライアント | OpenAPI 自動生成クライアント |
| ルーティング | React Router |

### 2.3 データベース

- **MySQL 8.x** (シングル DB + `TenantId` カラムフィルタ方式)
- ABP デフォルトのマルチテナント戦略 (Single Database, Multi-Tenant filter) を踏襲
- OEM 共通ナレッジは `TenantId IS NULL` で表現
- 開発者ローカルでは PC に **MySQL 8 を直接インストール**して使用 (Docker 不使用)

### 2.4 認証認可方針

- **Spring Authorization Server** を Spring Boot に組み込み、OpenIddict 相当の
  OAuth2 / OIDC 認可サーバを自前で持つ (.NET 版と同一構成)
- リソースサーバはモノリス内で同居 (将来的に切り出し可能な構成)

## 3. プロジェクト構造 (Maven マルチモジュール)

```text
AnomalyDetectionJava/
├── backend/
│   ├── pom.xml                            # 親 POM (parent, packaging=pom)
│   ├── domain-shared/                     # ABP Domain.Shared 相当
│   ├── domain/                            # ABP Domain 相当
│   ├── application-contracts/             # ABP Application.Contracts 相当
│   ├── application/                       # ABP Application 相当
│   ├── infrastructure/                    # ABP EntityFrameworkCore 相当
│   ├── web/                               # ABP HttpApi 相当
│   ├── auth-server/                       # Spring Authorization Server
│   ├── host/                              # ABP HttpApi.Host 相当 (起動 jar)
│   └── db-migrator/                       # ABP DbMigrator 相当
├── frontend/                              # React + Vite + TS + Ant Design
│   └── src/
│       ├── app/                           # ルーティング・レイアウト・プロバイダ
│       ├── modules/                       # 機能モジュール (cansignals, projects, …)
│       ├── shared/                        # 共通 UI / hook / API client
│       ├── api/                           # OpenAPI 自動生成クライアント
│       └── stores/                        # Zustand store
├── docs/
│   └── superpowers/specs/                 # 設計書 (spec)
└── README.md
```

### 3.1 モジュール依存方向 (循環禁止)

```text
domain-shared
   ↑
domain
   ↑
application-contracts
   ↑
application ──→ infrastructure ──→ host
   ↑                                ↑
   └──── web ────────────────────────┘
                                  ↑
                       auth-server, db-migrator も host で組み立て
```

横断的な依存制約は ArchUnit テストで検証する。

### 3.2 Spring Modulith 機能モジュール一覧

| モジュール | ABP 対応 |
| --- | --- |
| `cansignals` | CanSignals |
| `anomalydetection` | CanAnomalyDetectionLogic + 検出結果 |
| `cansspecification` | CanSpecification |
| `detectiontemplates` | DetectionTemplates |
| `projects` | Projects |
| `safety` | Safety (ISO 26262) |
| `knowledgebase` | KnowledgeBase |
| `oemtraceability` | OemTraceability |
| `similarpatternsearch` | SimilarPatternSearch |
| `integration` | Integration |
| `auditlogging` | AuditLogging |
| `multitenancy` | MultiTenancy |
| `identity` | Identity (ユーザ / ロール / 組織) |
| `permissions` | Volo.Abp.PermissionManagement |
| `settings` | Volo.Abp.SettingManagement |
| `features` | Volo.Abp.FeatureManagement |
| `backgroundjobs` | Volo.Abp.BackgroundJobs |
| `blobstoring` | Volo.Abp.BlobStoring.Database |

## 4. リポジトリ運用方針

- **既存 .NET 版とは独立した GitHub リポジトリ**として運用する
- 本フォルダ (`AnomalyDetectionJava/`) は将来そのまま新リポジトリのルートとなる
- `.NET 版のソース・履歴には一切影響を与えない`
- 仕様参照は `../AnomalyDetection/` の README とソースコードを参考にする

## 5. 開発ステータス

最終更新: 2026-04-29

### 5.1 マイルストーン進捗

| フェーズ | 状態 |
| --- | --- |
| ブレインストーミング (要件・前提整理) | **完了** |
| 設計書 (spec) 執筆 | **完了** |
| 実装計画 (plan) 作成 | M0〜M2 **完了** / M3〜M7 **未着手** (M4〜M7 は計画なしで直接実装) |
| **M0** (基盤セットアップ) | **完了** (tag: `m0-baseline`) |
| **M1** (Identity + Multi-Tenancy) | **完了** (tag: `m1-identity-multitenancy`) |
| **M2** (Spring Authorization Server) | **完了** (tag: `m2-spring-authorization-server`) |
| **M3** (横断機能: Permissions / Settings / Features / Audit / Jobs / BLOB / i18n) | **完了** (全横断機能バックエンド実装済。REST API: AuditLog/Settings/Features/Permissions/BlobStoring。フロントエンド管理ページ: 監査ログ/設定/フィーチャーフラグ/権限管理。85テスト全 PASS) |
| **M4** (コアドメイン移植) | **完了** (全10モジュール: CanSignal, CanSignalSpecification, DetectionTemplates, AnomalyDetection, Projects, Safety, KnowledgeBase, OemTraceability, SimilarPatternSearch, Integration) |
| **M5** (フロントエンド基盤) | **完了** (Zustand 5 / TanStack Query 5 / react-oidc-context 3 / Zod 3 / React Hook Form 7 / openapi-fetch 追加済。AuthProvider・QueryProvider・stores・api/client・shared/schemas・/callback ルート実装済) |
| **M6** (フロントエンド機能ページ) | **完了** (CAN信号・検出テンプレート・異常検出ロジック CRUD、インテグレーションエンドポイント一覧、Safety/KnowledgeBase/OemTraceability/CanSignalSpec 完全 CRUD 実装済。TanStack Query + apiFetch パターン統一) |
| **M7** (システム/性能テスト) | **完了** (91テスト全 PASS。ドメイン単体[19]: CanSignalTest・AnomalyDetectionLogicStatusTransitionTest・SimilarPatternSearchServiceTest / アプリサービス単体[12]: CanSignalAppServiceTest・AnomalyDetectionLogicAppServiceTest / API 統合[60]: CanSignalApiTest・DetectionTemplateApiTest・AnomalyDetectionLogicApiTest・SoftDeleteApiTest・ProjectsApiTest(6) 他。MapStruct @Mapper 導入・テストモジュール分散配置・Vitest+RTL+MSW フロントエンドテスト基盤・AuditLog HTTP ステータス捕捉・全ドメイン権限シード・i18n メッセージ拡充) |
| **M8** (automotive-safety トレーサビリティ強化 + CI/CD) | **完了** (Phase A: Safety/OemApproval/OemCustomization に状態遷移ガード・トレサビキー `feature_id`/`decision_id`/`if_impact`/`doc_sync_status` 等12列追加・Liquibase changeset 023・統合テスト8本。Phase B: `feature_id` 横断検索 API `GET /api/app/traceability/feature/{featureId}` + 統合テスト2本。Phase C-1: フロントエンド `usePermissions` / `RequirePermission` / メニュー権限フィルタ + ルート保護。Phase C-2: Safety 詳細 Drawer・OEM 突合 Drawer・トレサビバッジ。Phase D: GitHub Actions `backend-ci`/`frontend-ci`/`codeql`・Dependabot・PR Template) |

### 5.2 設計上の留意事項 (技術的負債候補)

- `projectsApi.ts` の手書き型 → バックエンド起動後に `npm run api:generate` で `src/api/schema.d.ts` 生成型へ置換 (現状は fetch + 手書き型で動作中)

### 5.3 進行順序の逸脱メモ

Projects ドメインは本実装済み (JPA Repository + Domain Aggregate + REST API + フロントエンド Create/Update/Delete + ProjectsApiTest 6テスト)。暫定 mock は完全に置き換え完了。

### 5.4 将来課題 (今回スコープ外、後続で対応)

- **Docker / docker-compose 構成** (個人開発のため当面はホスト直接実行)
- **本番デプロイ構成** (Nginx リバースプロキシ、TLS、サービス化、監視)
- **Redis キャッシュ・S3 BLOB ストレージへの切替**
- **Kafka / RabbitMQ などの外部メッセージブローカ連携**

## 6. 参考資料

- 既存 .NET 版 README: [../AnomalyDetection/README.md](../AnomalyDetection/README.md)
- 既存 .NET 版ソリューション: [../AnomalyDetection/AnomalyDetection.sln](../AnomalyDetection/AnomalyDetection.sln)
- 設計書: [docs/superpowers/specs/](docs/superpowers/specs/)
- 実装計画 (M0): [docs/superpowers/plans/2026-04-25-m0-baseline-setup.md](docs/superpowers/plans/2026-04-25-m0-baseline-setup.md)
