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

最終更新: 2026-04-26

### 5.1 マイルストーン進捗

| フェーズ | 状態 |
| --- | --- |
| ブレインストーミング (要件・前提整理) | **完了** |
| 設計書 (spec) 執筆 | **完了** |
| 実装計画 (plan) 作成 | M0 **完了** / M1〜M7 **未着手** |
| **M0** (基盤セットアップ) | **完了** (tag: `m0-baseline`) |
| **M1** (Identity + Multi-Tenancy) | **完了** (tag: `m1-identity-multitenancy`) |
| **M2** (Spring Authorization Server) | **未着手** |
| **M3** (横断機能: Permissions / Settings / Features / Audit / Jobs / BLOB / i18n) | **未着手** |
| **M4** (コアドメイン移植) | **Projects のみ in-memory mock 実装** (DDD レイヤー未経由・要 M1〜M3 完了後に本実装へ置換) |
| **M5** (フロントエンド基盤) | **部分的** (Vite + React 19 + TS + Ant Design 6 + React Router 7 完了。Zustand / TanStack Query / oidc-client-ts / Zod + React Hook Form / OpenAPI 自動生成 **未追加**) |
| **M6** (フロントエンド機能ページ) | **Projects 一覧/詳細のみ** (mock-first + backend-fallback) |
| **M7** (システム/性能テスト) | **未着手** |

### 5.2 設計上の留意事項 (技術的負債候補)

以下は M0 完了後に Plan 順序を逸脱して前倒しした作業に伴う**暫定実装**であり、後続フェーズで本実装に置き換える必要があります。

- `ProjectsAppService` の in-memory `List<ProjectDto>` 保持 → M1 完了後に **Domain Aggregate + JPA Repository** に置換 (現状 `domain/projects/` パッケージ自体が存在しない)
- `@PreAuthorize` / `PermissionDefinitionContributor` の後付け → M3a (permissions) 完了後
- Frontend に **Zustand / TanStack Query / oidc-client-ts / Zod + React Hook Form** を追加 → M5 本実装時
- **OpenAPI 自動生成パイプライン** (`openapi-typescript-codegen`) を構築し、`projectsApi.ts` の手書き型を生成型へ置換 → M5 本実装時
- `@types/react-router-dom` 5.3.3 の削除 (React Router 7 と不整合に同居) → 即時対応推奨

### 5.3 進行順序の逸脱メモ

Plan の正規順序は M0 → M1 → M2 → M3 → M4 → M5 → M6 → M7 ですが、現状は M0 完了後に **M5(部分) → M4(Projects mock) → M6(Projects 画面)** の順で前倒しが行われています。M1〜M3 (認証・マルチテナント・横断機能) を抜かしているため、Projects 関連の実装は本実装段階で広範な手直しが発生します。M1 (Identity + Multi-Tenancy) は完了。次は **M2 (Spring Authorization Server)** に進む。

### 5.4 将来課題 (今回スコープ外、後続で対応)

- **Docker / docker-compose 構成** (個人開発のため当面はホスト直接実行)
- **CI/CD パイプライン (GitHub Actions: `mvn verify` / ESLint / ビルド)**
- **本番デプロイ構成** (Nginx リバースプロキシ、TLS、サービス化、監視)
- **Redis キャッシュ・S3 BLOB ストレージへの切替**
- **Kafka / RabbitMQ などの外部メッセージブローカ連携**

## 6. 参考資料

- 既存 .NET 版 README: [../AnomalyDetection/README.md](../AnomalyDetection/README.md)
- 既存 .NET 版ソリューション: [../AnomalyDetection/AnomalyDetection.sln](../AnomalyDetection/AnomalyDetection.sln)
- 設計書: [docs/superpowers/specs/](docs/superpowers/specs/)
- 実装計画 (M0): [docs/superpowers/plans/2026-04-25-m0-baseline-setup.md](docs/superpowers/plans/2026-04-25-m0-baseline-setup.md)
