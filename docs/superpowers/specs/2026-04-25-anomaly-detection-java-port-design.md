# AnomalyDetection Java 移植 — 設計書 (Spec)

| 項目 | 値 |
| --- | --- |
| 文書 ID | `2026-04-25-anomaly-detection-java-port-design` |
| 作成日 | 2026-04-25 |
| ステータス | Draft (ユーザレビュー待ち) |
| 作成者 | ryuu1kyou + Claude Code |
| 関連リポジトリ (移植元) | [`../AnomalyDetection`](../../../../AnomalyDetection) |
| 関連リポジトリ (移植先) | `AnomalyDetectionJava` (本リポジトリ、将来 GitHub に独立登録) |

---

## 1. 目的とスコープ

### 1.1 目的

`.NET 10 + ABP vNext 9.3.5 + Angular 20` で実装されている CAN 異常検出管理システムを、
**Java 21 + Spring Boot 3.3 + React (Vite + Ant Design) + MySQL 8** に
**完全機能パリティで移植**する。

### 1.2 スコープ

#### 含む (In-Scope)

- 既存 ABP 版の全ドメインモジュールを Java 側で再現
  (`CanSignals`, `AnomalyDetection`, `CanSpecification`, `DetectionTemplates`,
  `Projects`, `Safety (ISO 26262)`, `KnowledgeBase`, `OemTraceability`,
  `SimilarPatternSearch`, `Integration`)
- ABP の横断機能を Java エコシステムの標準ライブラリで再現
  (`Identity`, `MultiTenancy`, `Permissions`, `Settings`, `Features`,
  `AuditLogging`, `BackgroundJobs`, `BlobStoring`, `Localization`,
  `DomainEvents`)
- Spring Authorization Server を組み込んだ OAuth 2.1 / OIDC 認可サーバ
- React + Ant Design による業務 UI (ABP の Angular UI と同等の操作性)
- ローカル開発環境セットアップ (Docker 不使用、ホスト直接実行)

#### 含まない (Out-of-Scope) — 将来課題

- Docker / docker-compose 化
- CI/CD パイプライン (GitHub Actions)
- 本番運用構成 (Nginx, TLS, サービス化, 監視, バックアップ)
- Redis 分散キャッシュ
- S3 互換 BLOB ストレージ
- Kafka / RabbitMQ 等の外部メッセージブローカ
- Microservice 分割 (auth-server を別プロセス化など)

### 1.3 前提と制約

- 個人開発 (1 名運用想定)
- `.NET 版とは独立した GitHub リポジトリ`として運用、`.NET 版のソースには影響を与えない`
- 開発 OS は Windows 11、ローカル MySQL 8 を直接インストールして使用
- フロント・バックエンドともに `./mvnw spring-boot:run` / `npm run dev` で起動

---

## 2. 技術スタック決定事項

### 2.1 バックエンド

| 領域 | 採用 | ABP 対応 |
| --- | --- | --- |
| 言語 / ランタイム | Java 21 LTS (Eclipse Temurin) | .NET 10 |
| ビルド | Maven 3.9 マルチモジュール (Maven Wrapper 同梱) | (.NET) `*.sln` |
| Web / モジュール | Spring Boot 3.3 + Spring Modulith | ABP モジュール |
| 認可サーバ | Spring Authorization Server (組み込み) | OpenIddict |
| リソースサーバ | Spring Security OAuth2 Resource Server | Volo.Abp.AspNetCore.Authentication |
| ORM | Spring Data JPA + Hibernate 6 | Entity Framework Core |
| DB マイグレーション | Liquibase (YAML 形式) | EF Core Migrations |
| マッピング | MapStruct | AutoMapper |
| 監査 | Spring Data JPA Auditing + Hibernate Envers | `IAuditedObject` / `AuditLog` |
| マルチテナント | Hibernate `@Filter` + AOP で `TenantId` 自動注入 | `IMultiTenant` |
| イベント | Spring Modulith Application Events | Volo.Abp.EventBus |
| バックグラウンドジョブ | Quartz Scheduler + ShedLock | Volo.Abp.BackgroundJobs |
| 機能管理 | Togglz + 自前 `feature_values` テーブル | Volo.Abp.FeatureManagement |
| 設定管理 | 自前 `settings` テーブル + Caffeine キャッシュ | Volo.Abp.SettingManagement |
| キャッシュ | Spring Cache + Caffeine | Volo.Abp.Caching |
| BLOB ストレージ | Spring Content (DB BLOB) | Volo.Abp.BlobStoring.Database |
| i18n | Spring `MessageSource` + `LocaleResolver` | Volo.Abp.Localization |
| WebSocket | Spring WebSocket + STOMP | SignalR |
| API ドキュメント | springdoc-openapi (OpenAPI 3) | ABP Swagger |
| テスト DB | MariaDB4j (組み込み MariaDB、JVM 内起動、Docker 不要) | xUnit + EF Core InMemory |
| テストフレームワーク | JUnit 5 + AssertJ + Mockito + ArchUnit + RestAssured | xUnit + Shouldly |
| 性能ベンチマーク | JMH | BenchmarkDotNet |
| ロギング | SLF4J + Logback + Logstash JSON | Serilog |

### 2.2 フロントエンド

| 領域 | 採用 |
| --- | --- |
| ビルド | Vite |
| 言語 | TypeScript (strict) |
| UI フレームワーク | React 18+ |
| UI ライブラリ | Ant Design (5.x) |
| 状態管理 | Zustand |
| サーバ状態 | TanStack Query |
| HTTP クライアント | OpenAPI 自動生成 (`openapi-typescript-codegen`) |
| ルーティング | React Router |
| 認証クライアント | `oidc-client-ts` (PKCE) |
| フォームバリデーション | Zod + React Hook Form (Ant Design `Form` と統合) |
| ユニットテスト | Vitest + React Testing Library + MSW |
| E2E テスト | Playwright |

### 2.3 データベース

- **MySQL 8.x** (本番・開発・テスト用)
- 開発者ローカルでは MySQL 8 をホスト OS に直接インストール
- テスト時は MariaDB4j (組み込み MariaDB) で代替
- マルチテナント戦略: `Single Database + tenant_id カラムフィルタ` (ABP デフォルト踏襲)
- 文字セット: `utf8mb4` / `utf8mb4_0900_ai_ci`

---

## 3. プロジェクト構造

### 3.1 リポジトリレイアウト

```text
AnomalyDetectionJava/
├── backend/
│   ├── pom.xml                            # 親 POM (parent, packaging=pom)
│   ├── domain-shared/                     # 列挙型 / 定数 / Localization key / 共通例外
│   ├── domain/                            # Entity / VO / Domain Service / Repository I/F / Domain Event
│   ├── application-contracts/             # DTO / AppService I/F / Permission 定義
│   ├── application/                       # UseCase / AppService 実装 / MapStruct / 認可属性
│   ├── infrastructure/                    # JPA Entity / Repository 実装 / Liquibase / Hibernate Filter / Tenant Resolver
│   ├── web/                               # REST Controller / 例外ハンドラ / OpenAPI 設定 / WebSocket(STOMP)
│   ├── auth-server/                       # Spring Authorization Server (OAuth2/OIDC, クライアント登録, JWT 鍵管理)
│   ├── host/                              # 起動アプリ (main, application.yml, プロファイル)
│   └── db-migrator/                       # 初期テナント / 初期権限 / シードデータ CLI
├── frontend/                              # React + Vite + TS + Ant Design
│   ├── src/
│   │   ├── app/                           # ルーティング・レイアウト・プロバイダ
│   │   ├── modules/                       # 機能モジュール (cansignals, projects, …)
│   │   ├── shared/                        # 共通 UI / hook / API client
│   │   ├── api/                           # OpenAPI 自動生成クライアント
│   │   └── stores/                        # Zustand store
│   ├── tests/
│   │   └── e2e/                           # Playwright E2E
│   ├── vite.config.ts
│   └── package.json
├── docs/
│   └── superpowers/specs/                 # 本設計書を含む各種 spec
├── CLAUDE.md
└── README.md
```

### 3.2 Maven 親 POM の主要構成

- `<parent>`: `spring-boot-starter-parent:3.3.x`
- `<properties>`: `java.version=21`, `mapstruct.version`, `liquibase.version`,
  `springdoc.version`, `quartz.version`, `mariadb4j.version`, `archunit.version`,
  `spring-modulith.version`, `togglz.version`
- `<dependencyManagement>`: Spring Modulith BOM, Spring Cloud BOM (必要時)
- `<modules>`: 9 サブモジュールを列挙
- ビルドプラグイン:
  - `spring-boot-maven-plugin` (host のみ実行可能 jar)
  - `maven-compiler-plugin` (`annotationProcessorPaths` に MapStruct + Lombok)
  - `jacoco-maven-plugin` (カバレッジ)
  - `spotless-maven-plugin` (Google Java Format)
  - `spotbugs-maven-plugin` + FindSecBugs
  - `dependency-check-maven` (OWASP)
  - `maven-enforcer-plugin`

### 3.3 モジュール依存方向 (循環禁止)

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
                       auth-server, db-migrator も host が組み立て
```

横断的な依存制約 (`web` から `infrastructure` 直接参照禁止など) は **ArchUnit テスト**で検証。

### 3.4 Spring Modulith 機能モジュール

各 Maven モジュール内のパッケージを Spring Modulith のアプリケーションモジュールとして
扱う。ABP の機能領域に対応:

| Spring Modulith モジュール | ABP 対応 |
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

---

## 4. ABP 機能 → Java 実装マッピング

### 4.1 レイヤー / ベースクラス

| ABP | Java/Spring 実装 |
| --- | --- |
| `AggregateRoot<TKey>` | 抽象クラス `AggregateRoot<ID>` (`@MappedSuperclass`)。`addLocalEvent()` でドメインイベント蓄積、`@PostPersist`/`@PostUpdate` で publish |
| `Entity<TKey>` / `IEntity` | `@MappedSuperclass` + ID 基準 `equals`/`hashCode` |
| `IFullAuditedObject` | 抽象 `FullAuditedEntity` (`createdAt`, `createdBy`, `lastModifiedAt`, `lastModifiedBy`, `deletedAt`, `deletedBy`, `isDeleted`) + Spring Data JPA Auditing + `@SQLDelete` でソフト削除 |
| `IMultiTenant` | インターフェース `MultiTenant` (`UUID getTenantId()`) + Hibernate `@Filter` |
| `IDomainService` | アノテーション `@DomainService` (`@Component` メタ) |
| `IApplicationService` | アノテーション `@ApplicationService` (`@Service` + `@Transactional` 既定) |
| `CrudAppService<…>` | ジェネリック抽象 `CrudApplicationService` (CRUD + 認可 + マッピング + ページング) |
| `IRepository<T, TKey>` | Spring Data JPA `JpaRepository` を継承する各機能の Repository インターフェース |
| `IGuidGenerator` | Bean `GuidGenerator` (UUID v7 既定、差替可) |
| `ICurrentUser` | Bean `CurrentUser` (Spring Security `Authentication` → Claim 抽出) |
| `ICurrentTenant` | Bean `CurrentTenant` + `TenantScopedExecutor` (`change(tenantId, () -> {...})`) |

### 4.2 横断機能

| ABP | Java/Spring 実装 |
| --- | --- |
| ドメインイベント (`IDomainEvents`) | Spring Modulith `ApplicationEvents` (`event_publication` テーブルに自動永続化) |
| 分散イベント (アウトボックス) | Spring Modulith Externalization (将来 Kafka/RabbitMQ 用、初期は in-process) |
| `IUnitOfWork` | `@Transactional` + Spring Modulith のトランザクション境界 |
| 認可 (`[Authorize]` / `IPermissionChecker`) | Spring Security `@PreAuthorize("hasAuthority('CanSignals.Edit')")` + 自前 `PermissionEvaluator` |
| Permission 定義 (`PermissionDefinitionProvider`) | 各機能モジュールが `PermissionDefinitionContributor` Bean を実装 → 起動時に `permission_definitions` テーブルへ同期 |
| Setting (`ISettingProvider`) | `settings` テーブル (Global / Tenant / User スコープ) + `SettingProvider` Bean + Caffeine |
| Feature (`IFeatureChecker`) | Togglz + `feature_values` テーブル (テナント別オーバーライド) |
| 監査ログ (`AuditLog`) | Spring AOP インターセプタ + `audit_logs` テーブル + Hibernate Envers でエンティティ履歴 |
| ローカライズ (`IStringLocalizer`) | Spring `MessageSource` + `LocaleContextHolder` (ヘッダ/Cookie で切替) |
| バックグラウンドジョブ (`IBackgroundJobManager`) | Quartz Scheduler + ShedLock (DB ロック) + `BackgroundJob` 抽象 |
| BLOB ストレージ (`IBlobContainer`) | Spring Content Commons + Spring Content JPA |
| キャッシュ (`IDistributedCache<T>`) | Spring Cache 抽象 + Caffeine (将来 Redis 切替可) |
| メール (`IEmailSender`) | Spring `JavaMailSender` + `email_templates` テーブル (DEV では `NullEmailSender`) |

### 4.3 認証・OAuth2

| ABP / OpenIddict | Java/Spring 実装 |
| --- | --- |
| 認可サーバ (`/connect/token`, `/connect/authorize`) | Spring Authorization Server `OAuth2AuthorizationServerConfigurer` (デフォルト `/oauth2/token`, `/oauth2/authorize`) |
| クライアント登録 (`OpenIddictApplicationDescriptor`) | `RegisteredClientRepository` (JDBC, `oauth2_registered_client`) |
| スコープ / 同意 | `OAuth2AuthorizationConsentService` (JDBC) |
| ID/アクセストークン (JWT) | RSA 鍵を `host/src/main/resources/keys/` または KMS から読込み、`JwtEncoder` で署名 |
| ABP Identity | `users`, `roles`, `user_roles`, `organization_units`, `user_organization_units` テーブル + `UserDetailsService` |
| 多段ログイン (テナント解決) | `TenantResolver` (サブドメイン / ヘッダ / クレーム) → 認可フロー前にテナントコンテキスト確立 |

### 4.4 マルチテナント実装の詳細

1. **テナント解決**: `TenantResolutionFilter` がリクエストから
   `__tenant` ヘッダ → サブドメイン → JWT `tenantid` クレームの順で解決し
   `CurrentTenantHolder` (Request scope Bean) にセット
2. **データフィルタ**: Hibernate `@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId OR tenant_id IS NULL")`
   を全 `MultiTenant` エンティティに付与し、セッション開始時に有効化
3. **書き込み時の自動付与**: JPA `@PrePersist` フック (`MultiTenantEntityListener`) が
   `CurrentTenant.getId()` を `tenant_id` に注入
4. **Host モード**: ホスト管理者は `CurrentTenant.None` で操作 (フィルタを一時無効化)
5. **クロステナント参照** (OEM 共通ナレッジ): `tenant_id IS NULL` をフィルタ条件に含めることで
   自動的に共有データへアクセス可

### 4.5 OpenAPI と React 連携

- バックエンド: `springdoc-openapi-starter-webmvc-ui` で `/v3/api-docs` 生成
- フロントエンド: Vite ビルド時に `openapi-typescript-codegen` で TS クライアント自動生成
  → `frontend/src/api/` に出力
- 認証: PKCE + Authorization Code フロー (`oidc-client-ts` 採用)

---

## 5. データベース設計

### 5.1 命名規則

| 項目 | 規則 |
| --- | --- |
| テーブル名 | `snake_case` 複数形 (例: `can_signals`, `audit_logs`) |
| カラム名 | `snake_case` (例: `tenant_id`, `created_at`) |
| 主キー | `id` (`BINARY(16)` で UUID v7) |
| 外部キー | `<相手テーブル単数形>_id` |
| 文字セット | `utf8mb4` / `utf8mb4_0900_ai_ci` |
| 監査カラム | `created_at`, `created_by`, `last_modified_at`, `last_modified_by`, `is_deleted`, `deleted_at`, `deleted_by` |
| マルチテナントカラム | `tenant_id BINARY(16) NULL` (NULL は OEM 共通) |

### 5.2 Liquibase changelog 構成

```text
infrastructure/src/main/resources/db/changelog/
├── db.changelog-master.yaml                # 親 changelog
├── 0000-baseline/                          # 初期スキーマ
│   ├── 001-identity.yaml                   # users, roles, claims, organization_units
│   ├── 002-multitenancy.yaml               # tenants, tenant_connection_strings
│   ├── 003-oauth2-server.yaml              # oauth2_registered_client, oauth2_authorization, oauth2_authorization_consent
│   ├── 004-permissions.yaml                # permission_grants, permission_definitions
│   ├── 005-settings.yaml                   # settings (Global/Tenant/User)
│   ├── 006-features.yaml                   # feature_values
│   ├── 007-audit.yaml                      # audit_logs, audit_log_actions, entity_changes, entity_property_changes
│   ├── 008-background-jobs.yaml            # background_jobs
│   ├── 009-blob-storing.yaml               # blob_containers, blobs
│   ├── 010-modulith-events.yaml            # event_publication
│   ├── 011-domain-cansignals.yaml          # can_signals, can_signal_specifications, can_system_categories
│   ├── 012-domain-detection.yaml           # can_anomaly_detection_logics, anomaly_detection_results, ...
│   ├── 013-domain-templates.yaml
│   ├── 014-domain-projects.yaml            # projects, vehicle_phases
│   ├── 015-domain-safety.yaml              # safety_classifications, safety_traceability_links
│   ├── 016-domain-knowledge.yaml
│   ├── 017-domain-oem-traceability.yaml
│   └── 018-domain-similar-pattern.yaml
└── 0001-changes/                           # 以後の差分 (`YYYYMMDD-NN-xxx.yaml`)
```

各 changeSet には `author`, `id`, `comment` を必須化、`<rollback>` で巻戻し手順を明記。
起動時に Liquibase が自動実行される (`spring.liquibase.enabled=true`)。

### 5.3 主要テーブル

#### Identity / Multi-Tenancy

- `tenants` (id, name, normalized_name, is_active, ...)
- `tenant_connection_strings` (tenant_id, name, value)
- `users` (id, tenant_id, user_name, normalized_user_name, email, password_hash, ...)
- `roles` (id, tenant_id, name, normalized_name, is_static, is_default, ...)
- `user_roles`, `user_claims`, `role_claims`, `user_logins`, `user_tokens`
- `organization_units` (`code` カラムで階層パス表現)

#### OAuth2 (Spring Authorization Server 公式スキーマ)

- `oauth2_registered_client`
- `oauth2_authorization`
- `oauth2_authorization_consent`

#### 横断機能

- `permission_grants` (name, provider_name, provider_key, tenant_id)
- `permission_definitions` (起動時に Contributor が同期)
- `settings` (name, value, provider_name, provider_key)
- `feature_values` (name, value, provider_name, provider_key)
- `audit_logs` + `audit_log_actions` + `entity_changes` + `entity_property_changes`
- `background_jobs` (id, job_name, job_args, try_count, next_try_time, is_abandoned, ...)
- `blob_containers` + `blobs`
- `event_publication` (Spring Modulith 公式スキーマ)

#### ドメイン (代表例)

- `can_signals` (id, tenant_id, signal_identifier, name, system_category, ..., 監査カラム)
- `can_signal_specifications` (id, can_signal_id, ..., physical_value_conversion_*)
- `can_system_categories` (id, name, description)
- (他ドメインモジュールも同形のテーブル群を持つ)

### 5.4 初期データ投入 (`db-migrator`)

ABP の `IDataSeedContributor` 相当を Spring 側で再現。

- `db-migrator` モジュールは独立した Spring Boot CLI アプリ (`CommandLineRunner`)
- 各機能モジュールが `DataSeedContributor` を `@Component` として提供
- 起動時に Liquibase 実行 → 全 `DataSeedContributor` を `@Order` 順に実行
- 投入される初期データ:
  1. デフォルトテナント (Host) と デフォルト管理者 (`admin` / 環境変数 `INITIAL_ADMIN_PASSWORD`)
  2. 静的ロール (`admin`, `tenant-admin`, `engineer`, `viewer`)
  3. OAuth2 クライアント (`anomaly-web`, `anomaly-swagger`, `anomaly-internal`)
  4. Permission 定義の DB 同期
  5. 既定 Setting (タイムゾーン、デフォルト言語、メール送信元)
  6. CAN 信号の標準カテゴリ (CanSystemCategory マスター)
  7. 既定の異常検出テンプレート
- 各 seed は冪等性を保証 (`existsByName` 等で重複チェック後に挿入)

### 5.5 マイグレーション運用方針

- 開発時: `host` 起動時に Liquibase が自動実行
- テスト時: MariaDB4j 起動後に Liquibase が自動実行 (本番と同じ経路)
- 本番時 (将来): `db-migrator` を独立して 1 回実行する init 実行スタイル

---

## 6. 認証・認可フロー

### 6.1 全体フロー (OAuth 2.1 Authorization Code + PKCE)

```text
┌────────────┐  1. /authorize (PKCE challenge, tenant=oem-a)   ┌──────────────────────┐
│ React SPA  │ ───────────────────────────────────────────────→ │ auth-server          │
│ (Vite)     │  2. ログイン画面 (Spring Security FormLogin)      │ (Spring Authorization │
│            │ ←───────────────────────────────────────────────  │  Server)             │
│            │  3. 認可コード (リダイレクト)                      │                      │
│            │ ───────────────────────────────────────────────→ │                      │
│            │  4. /token (code + PKCE verifier) → JWT access+refresh                  │
│            │ ←───────────────────────────────────────────────  └──────────────────────┘
│            │
│            │  5. Authorization: Bearer <jwt>                  ┌──────────────────────┐
│            │ ───────────────────────────────────────────────→ │ web (Resource Server)│
│            │  6. JSON                                          │ /api/app/can-signal …│
│            │ ←───────────────────────────────────────────────  └──────────────────────┘
└────────────┘
```

- 認可サーバとリソースサーバは **同一 host モジュールに同居** (将来切り出し可能)
- React SPA は `oidc-client-ts` で PKCE フローを駆動
- 開発時 `redirect_uri = http://localhost:5173/auth/callback`

### 6.2 OAuth2 クライアント定義 (`db-migrator` で投入)

| client_id | grant_types | redirect_uris | scopes | 用途 |
| --- | --- | --- | --- | --- |
| `anomaly-web` | `authorization_code`, `refresh_token` | `<frontend>/auth/callback` | `openid profile email roles AnomalyDetection` | React SPA (Public, PKCE 必須) |
| `anomaly-swagger` | `authorization_code` | `<host>/swagger/oauth2-redirect.html` | `openid AnomalyDetection` | Swagger UI |
| `anomaly-internal` | `client_credentials` | — | `AnomalyDetection.Internal` | サーバ間連携 (将来用) |

### 6.3 JWT トークン構造 (アクセストークン)

```json
{
  "iss": "https://auth.anomaly-detection.local",
  "sub": "<userId(UUID)>",
  "aud": "AnomalyDetection",
  "exp": 1714060000,
  "iat": 1714056400,
  "scope": "openid profile email roles AnomalyDetection",
  "tenantid": "<tenantId(UUID) or null>",
  "preferred_username": "alice",
  "email": "alice@oem-a.com",
  "roles": ["tenant-admin", "engineer"],
  "oi_prst": ["AnomalyDetection.CanSignals.Edit", "AnomalyDetection.Projects.View"]
}
```

- `tenantid` クレームでテナント識別 (host 管理者は `null`)
- `oi_prst` (= permission strings) で **解決済み権限**を含める
  → リソースサーバ側は再 DB アクセス不要
- 権限の DB 集計は **トークン発行時のみ** (`OAuth2TokenCustomizer<JwtEncodingContext>` 実装)
- アクセストークン有効期間: **30 分**、リフレッシュトークン: **14 日**
  (絶対 / スライディング切替可、設定値は `settings` テーブルで管理)

### 6.4 テナント解決の優先順位

#### ログイン時 (auth-server 側)

1. `/authorize` のクエリパラメータ `__tenant=<name>` または `tenant_id`
2. リクエストのサブドメイン (`oem-a.anomaly.local` → tenant `oem-a`)
3. ログインフォームの「テナント」入力欄
4. いずれも未指定なら **Host テナント** (admin ユーザのみ許可)

→ 解決後、`tenants.id + users.user_name` の複合一意キーで認証

#### リソースアクセス時 (web 側)

1. JWT の `tenantid` クレーム (**最優先・改ざん不可**)
2. クレームと `__tenant` ヘッダが食い違う場合は **403 Forbidden**

→ 解決後 `CurrentTenantHolder` にセットし、Hibernate Filter を有効化

### 6.5 Permission

#### Permission 定義例

```java
@Component
public class CanSignalsPermissionDefinitionContributor
    implements PermissionDefinitionContributor {

  public static final String GROUP   = "AnomalyDetection.CanSignals";
  public static final String DEFAULT = GROUP + ".Default";   // 一覧/閲覧
  public static final String CREATE  = GROUP + ".Create";
  public static final String EDIT    = GROUP + ".Edit";
  public static final String DELETE  = GROUP + ".Delete";

  @Override
  public void define(PermissionDefinitionContext context) {
    var group = context.addGroup(GROUP, "CAN信号管理");
    var def = group.addPermission(DEFAULT);
    def.addChild(CREATE);
    def.addChild(EDIT);
    def.addChild(DELETE);
  }
}
```

#### Permission チェック

- メソッドレベル: `@PreAuthorize("hasAuthority('AnomalyDetection.CanSignals.Edit')")`
- 動的判定: `permissionChecker.isGranted(CanSignalsPermissions.EDIT)`
- 機能フラグ連動: `@RequiresFeature("EnableSafetyModule")` で
  Feature 無効テナントには `404 Not Found` 返却

### 6.6 セッション・トークン保存戦略 (フロント)

| 項目 | 採用 | 理由 |
| --- | --- | --- |
| アクセストークン | メモリ (`oidc-client-ts` 既定) | XSS リスク低減 |
| リフレッシュトークン | HttpOnly + Secure + SameSite=Lax Cookie | XSS で盗まれない |
| サイレントリフレッシュ | `iframe` 経由 `prompt=none` 認可リクエスト | アクセストークン期限切れ前に自動更新 |
| ログアウト | `/connect/endsession` (= Spring AS の `/logout`) → メモリトークン破棄 + Cookie 削除 | サーバ側セッションも破棄 |

### 6.7 CORS / CSRF

- **CORS**: `auth-server` と `web` の両方で `app.cors.allowed-origins` 設定値を `settings` テーブルから読込み
- **CSRF**: REST API では JWT ベースのため無効 (`http.csrf().disable()`)。
  認可サーバの FormLogin / `/logout` では Spring Security 既定の CSRF を有効化
- 認可サーバの `/login` は最小 Thymeleaf テンプレートを `auth-server` 側に同居

### 6.8 失敗パターンと対応

| パターン | レスポンス |
| --- | --- |
| 無効/期限切れ JWT | `401 Unauthorized` + `WWW-Authenticate: Bearer error="invalid_token"` |
| 権限不足 | `403 Forbidden` + `error.code = "AnomalyDetection:010001"` |
| テナント不一致 | `403 Forbidden` + `error.code = "Volo.Abp.MultiTenancy:010001"` |
| 機能フラグ無効 | `404 Not Found` (機能の存在を露出させない) |
| アカウントロック | `400 Bad Request` + `error.code = "Volo.Abp.Identity:010003"` |

---

## 7. 移植順序・マイルストーン

完全機能パリティ移植は規模が大きいため、**段階的に動作する成果物**を積み上げる
方式で進める。各フェーズの最後にマイルストーンを置き、デモ可能な状態にする。

### 7.1 Phase 0 / 基盤セットアップ (M0)

- Maven マルチモジュール雛形 (9 モジュール) 作成
- 親 POM (Spring Boot 3.3 BOM, Java 21, MapStruct, Lombok, Spring Modulith BOM)
- `host` で空 Spring Boot アプリ起動 (`/actuator/health` 返却)
- `infrastructure` で MySQL 8 接続 + Liquibase で空スキーマ初期化
- ArchUnit テストでモジュール境界検証
- **M0 成果物**: 空アプリ起動、Liquibase 動作、ArchUnit 緑

### 7.2 Phase 1 / Identity + Multi-Tenancy (M1)

- `multitenancy` モジュール: `tenants` + Tenant Aggregate + `TenantResolutionFilter` + Hibernate Filter 基盤
- `identity` モジュール: User / Role / OrganizationUnit
- `db-migrator` から **デフォルトテナント / admin / 静的ロール**投入
- `web` に Tenant/User CRUD 公開 (一旦は認可なし)
- **M1 成果物**: Tenant/User CRUD 動作、`tenant_id` フィルタ機能、ArchUnit 緑

### 7.3 Phase 2 / Spring Authorization Server (M2)

- `auth-server` モジュールに Spring Authorization Server 組み込み
- `oauth2_*` JDBC スキーマを Liquibase 投入
- `db-migrator` で `anomaly-web` / `anomaly-swagger` クライアント投入
- `OAuth2TokenCustomizer` で `tenantid` / `roles` / `oi_prst` クレーム付与
- 最小 Thymeleaf ログイン画面 (テナント選択フィールドあり)
- リソースサーバ側の JWT 検証設定 (`web` モジュール)
- **M2 成果物**: PKCE 認可コードフローが通り、JWT で保護 API を叩ける

### 7.4 Phase 3 / 横断機能モジュール (M3)

| ステージ | 内容 |
| --- | --- |
| 3a | `permissions` (PermissionDefinitionContributor / `permission_grants` / `@PreAuthorize`) |
| 3b | `settings` + `features` (Caffeine キャッシュ込み) |
| 3c | `auditlogging` (AOP インターセプタ + Hibernate Envers) |
| 3d | `backgroundjobs` (Quartz + ShedLock) + `blobstoring` + `localization` |

**M3 成果物**: 監査・権限・設定・機能・ジョブ・BLOB・i18n すべて動作する基盤完成

### 7.5 Phase 4 / コアドメイン移植 (M4)

ABP 側のドメイン依存関係を踏まえた順序で実装。

| 順 | モジュール | 依存 |
| --- | --- | --- |
| 1 | `cansspecification` | - |
| 2 | `cansignals` | `cansspecification` |
| 3 | `detectiontemplates` | `cansignals` |
| 4 | `anomalydetection` | `cansignals`, `detectiontemplates` |
| 5 | `projects` | `cansignals`, `anomalydetection` |
| 6 | `safety` (ISO 26262) | `projects`, `anomalydetection` |
| 7 | `oemtraceability` | `projects`, `safety` |
| 8 | `knowledgebase` | `projects`, `anomalydetection` |
| 9 | `similarpatternsearch` | `cansignals`, `anomalydetection` |
| 10 | `integration` | 上記全て |

各モジュール:

1. JPA エンティティ + Liquibase changelog
2. Repository + Domain Service
3. AppService + DTO + MapStruct + Permission 定義
4. REST Controller + OpenAPI スキーマ
5. ユニット/統合テスト (ABP 側既存テスト内容を移植)

**M4 成果物**: 全 10 ドメインモジュールの REST API 完成、Swagger UI で全エンドポイント確認可能

### 7.6 Phase 5 / フロントエンド基盤 (M5)

- Vite + TS + React 18 雛形
- Ant Design + テーマ設定 (Angular Material 配色を踏襲)
- `oidc-client-ts` で PKCE フロー
- TanStack Query + Zustand
- OpenAPI 自動生成パイプライン
- 共通レイアウト (Sider + Header + Content)、認可ガードルート、テナント切替 UI、i18n (ja/en)
- **M5 成果物**: ログイン → ダッシュボード骨組み → API 接続が動作

### 7.7 Phase 6 / フロントエンド機能ページ (M6)

ABP の Angular 画面と機能等価な React 画面を実装。順序は M4 と同じ。

各機能ページの構成:

- 一覧 (Ant Design `Table` 自前ラッパー、フィルタ・ソート・ページング)
- 詳細 (Drawer / Modal)
- 作成・編集フォーム (Ant Design `Form` + Zod バリデーション)
- 削除確認 (`Popconfirm`)

**M6 成果物**: Angular 版と同一の業務シナリオを React 版で完遂可能

### 7.8 Phase 7 / システムテスト・パフォーマンス (M7)

- `AnomalyDetection.SystemIntegrationTests` 相当を `host` の `@SpringBootTest` + RestAssured で再現
- `AnomalyDetection.PerformanceBenchmarks` 相当を JMH で再現
- 主要シナリオの Playwright E2E (Angular Cypress テストを移植)
- **M7 成果物**: 既存テストカバレッジ同等以上、性能ベンチマーク結果

### 7.9 並列化のヒント

- Phase 4 のドメインモジュールは依存関係さえ守ればチーム内で並列可能
- Phase 5 のフロント基盤は M2 完了後に着手可能 (Phase 3 と並行可)
- Phase 6 の機能ページはバックエンドの該当 API 完成 (M4 部分完了) 次第着手可

### 7.10 マイルストーン全体像

```text
M0 ─→ M1 ─→ M2 ─→ M3 ─→ M4 ──┬─→ M7
                          ↑   │
                          M5 ─┴─→ M6 ─→ (M7 へ合流)
```

---

## 8. テスト戦略・コード品質

### 8.1 既存 .NET テストプロジェクトの対応

| .NET 側 | Java 側 | 配置 |
| --- | --- | --- |
| `AnomalyDetection.Domain.Tests` | 純粋ドメインユニットテスト (Spring 不使用) | `domain/src/test/java/...` |
| `AnomalyDetection.Application.Tests` | `@SpringBootTest` スライス + MariaDB4j | `application/src/test/java/...` |
| `AnomalyDetection.EntityFrameworkCore.Tests` | `@DataJpaTest` + MariaDB4j | `infrastructure/src/test/java/...` |
| `AnomalyDetection.SystemIntegrationTests` | `@SpringBootTest(webEnvironment=RANDOM_PORT)` + MariaDB4j + RestAssured | `host/src/test/java/...` |
| `AnomalyDetection.PerformanceBenchmarks` | JMH | `host/src/jmh/java/...` |
| `AnomalyDetection.TestBase` | 共通テスト基盤 | `domain-shared/src/testFixtures/java/...` |
| Angular Jasmine/Karma | Vitest + RTL | `frontend/src/**/*.test.tsx` |
| Angular Cypress | Playwright | `frontend/tests/e2e/` |

### 8.2 テストピラミッド

```text
                   ╱╲   E2E (Playwright)         主要 5-10 シナリオ
                  ╱  ╲
                 ╱────╲ システム統合 (RestAssured)  機能ごと 2-3 件
                ╱      ╲
               ╱        ╲ 統合テスト (@SpringBootTest スライス + MariaDB4j)
              ╱──────────╲
             ╱            ╲ Web/Repository スライス (@WebMvcTest / @DataJpaTest)
            ╱──────────────╲
           ╱                ╲  ユニットテスト (純ドメイン JUnit 5 + AssertJ + Mockito)
          ╱──────────────────╲
```

### 8.3 バックエンドテスト方針

#### ユニットテスト (Domain 層)

- Spring コンテキストを起動しない純粋 JUnit 5
- Aggregate / VO / Domain Service の不変条件を網羅
- AssertJ (`assertThat(...)`)
- ABP の `Should_*` 命名を踏襲: `should_publish_event_when_signal_is_published()`
- カバレッジ目標: **Domain モジュール 90% 以上**

#### Repository / Persistence テスト

- `@DataJpaTest` + MariaDB4j (`MariaDB4jExtension` を `domain-shared/testFixtures` に用意)
- Liquibase changelog も MariaDB4j で実行 (本番と同じ経路)
- Hibernate Filter (テナントフィルタ) の動作テストを必須化

#### Application Service テスト

- `@SpringBootTest(classes = ApplicationTestConfig.class)`
- MariaDB4j インスタンスはテストモジュール内で共有 (起動コスト削減)
- 認可・テナントコンテキストは `@WithMockJwt(tenantId, roles, permissions)` カスタムアノテーションで注入
- `@Transactional` でロールバック (高速)、イベント発行系は `@Commit` で別途検証

#### Web / API テスト

- `@WebMvcTest` + MockMvc (薄いコントローラ層検証)
- システム統合は RestAssured (`@SpringBootTest(webEnvironment=RANDOM_PORT)`)
- OpenAPI スキーマと実レスポンスの整合性は springdoc のテストツールで自動検証

#### アーキテクチャテスト (ArchUnit)

- 必須テスト (毎ビルド実行):
  - `web` → `infrastructure` 直接参照禁止
  - `domain` から Spring 注入禁止 (`@Autowired` 検出 → 失格)
  - 各機能モジュールの `internal` パッケージは他モジュールから参照禁止
  - JPA エンティティは `infrastructure` モジュール内のみ
  - DTO は `application-contracts` 以外で定義禁止

#### Spring Modulith テスト

```java
@ModularityTest
class ModularityTests {
  @Test void verifyModuleStructure(ApplicationModules modules) {
    modules.verify();
  }
  @Test void documentModules(ApplicationModules modules) {
    new Documenter(modules).writeDocumentation();
  }
}
```

モジュール構成図を毎ビルドで再生成し、Pull Request 差分で構造変化を可視化。

#### パフォーマンステスト (JMH)

クリティカルパスのみ:

- 異常検出ロジック評価 (`DetectionLogic.evaluate(signal)`)
- 大量検出結果の集計 (Statistics)
- マルチテナントフィルタの O(N) 影響測定

### 8.4 フロントエンドテスト方針

| 種類 | ツール | 対象 |
| --- | --- | --- |
| ユニット | Vitest + RTL | hook / store / utility |
| コンポーネント | Vitest + RTL + MSW | UI コンポーネント (API モック) |
| E2E | Playwright | 主要 5-10 業務シナリオ。**手動起動した host (Spring Boot) + ローカル MySQL** に対して実行 (Docker 不使用) |

- MSW で API モック (OpenAPI スキーマから自動生成可)
- Storybook は導入しない (YAGNI)

### 8.5 コード品質ツール (Maven プラグイン)

| ツール | 役割 | 失格条件 |
| --- | --- | --- |
| Spotless + Google Java Format | フォーマット強制 | 差分があれば失敗 |
| Checkstyle | 命名・基本ルール | エラー 0 件必須 |
| SpotBugs + FindSecBugs | バグ・セキュリティ | High/Medium 警告 0 件必須 |
| Jacoco | カバレッジ集計 | Domain ≥ 90%, Application ≥ 80%, 全体 ≥ 70% |
| OWASP Dependency-Check | 依存ライブラリ脆弱性 | CVSS 7+ 検出時失敗 |
| Maven Enforcer | バージョン重複・循環依存 | 違反時失敗 |
| ArchUnit | アーキテクチャルール | 違反時失敗 (テスト扱い) |

フロントエンド側:

- ESLint + `@typescript-eslint` + `eslint-plugin-react-hooks` (エラー 0)
- Prettier (フォーマット差分 0)
- TypeScript strict (`tsc --noEmit` 緑)

### 8.6 テストデータ戦略

- Builder パターン: `TestData.canSignal().withTenantId(...).build()`
- フィクスチャは `domain-shared/src/testFixtures/java/.../testdata/` に集約
- マルチテナント検証は **2 テナント + Host** の固定セットを必ず用意

---

## 9. 開発環境・運用

### 9.1 ローカル前提

| ツール | バージョン | 備考 |
| --- | --- | --- |
| Java JDK | 21 LTS (Eclipse Temurin) | `JAVA_HOME` 必須 |
| Maven | 3.9.x (Maven Wrapper `./mvnw` 同梱) | バージョン固定 |
| Node.js | 20.x LTS | `frontend/.nvmrc` |
| MySQL | 8.x (PC に直接インストール) | Windows MSI / Homebrew / apt |
| MySQL Workbench | 任意 | DB 操作 GUI |
| IDE | IntelliJ IDEA / VS Code | 共通設定を `.idea/` `.vscode/` で共有 |
| Docker | **不要** | (Testcontainers の代わりに MariaDB4j を採用) |

### 9.2 ローカル開発ワークフロー

1. **MySQL 起動** (Windows サービス / `brew services start mysql`)
2. 初回のみ DB 作成:

   ```sql
   CREATE DATABASE anomaly_detection
     CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
   ```

3. 初期データ投入:

   ```bash
   ./mvnw -pl db-migrator -am spring-boot:run
   ```

4. バックエンド起動:

   ```bash
   ./mvnw -pl host -am spring-boot:run -Dspring-boot.run.profiles=local
   ```

5. フロントエンド起動:

   ```bash
   cd frontend && npm run dev
   ```

6. テスト:

   ```bash
   ./mvnw -B verify        # バックエンド全テスト + 静的解析
   cd frontend && npm test
   cd frontend && npm run e2e
   ```

- Spring DevTools でクラスパス変更時の自動リロード
- バックエンド: `http://localhost:44397` (既存 .NET 版とポートを揃える)
- Swagger UI: `http://localhost:44397/swagger-ui.html`
- フロント: `http://localhost:5173` (`vite.config.ts` で `/api` をバックエンドにプロキシ)

### 9.3 Spring プロファイル

| プロファイル | 用途 | 主な差分 |
| --- | --- | --- |
| `local` | 開発者ローカル | DevTools 有効、CORS 寛容、SQL ログ ON、`NullEmailSender` |
| `test` | 自動テスト | MariaDB4j 接続、Liquibase 即実行、ログ DEBUG |
| `prod` | 本番 (将来) | TLS、機密は環境変数、SQL ログ OFF |

`application.yml` をベースに `application-{profile}.yml` で差分のみ記述 (12-factor 準拠)。

### 9.4 環境変数

ローカルは `application-local.yml` に直書き OK (個人用)。
ただし以下は必ず環境変数で:

| 変数 | 例 | 用途 |
| --- | --- | --- |
| `INITIAL_ADMIN_PASSWORD` | (秘) | `db-migrator` 初回シード用、リポジトリにコミット禁止 |
| `JWT_KEY_LOCATION` (将来) | `file:/path/to/jwt-private.pem` | RSA 秘密鍵パス |
| `MAIL_PASSWORD` (将来) | (秘) | SMTP 認証 |

### 9.5 監視・運用 (個人用最小構成)

- ヘルスチェック: Spring Actuator `/actuator/health`
- ログ: Logback デフォルトでコンソール + ローカルファイル `logs/anomaly-detection.log`
- 監視・バックアップ・通知: **将来課題** (現時点では実装しない)

### 9.6 既存 ABP 版から流用するアセット

| 流用元 | 流用先 | 形式 |
| --- | --- | --- |
| `etc/` の i18n リソース | `domain-shared/src/main/resources/i18n/` | JSON → properties に変換 |
| 既存 README の業務説明 | `frontend/docs/` | 翻訳/転載 |
| `etc/` のドメイン辞書 (CAN 信号用語) | `docs/glossary.md` | Markdown |
| Angular CSS テーマ変数 | Ant Design `ConfigProvider` の `theme.token` | 値の変換のみ |

---

## 10. 将来課題 (Out-of-Scope の保留事項)

| 区分 | 項目 | 備考 |
| --- | --- | --- |
| インフラ | Docker / docker-compose 化 | 個人用で当面不要 |
| インフラ | CI/CD パイプライン (GitHub Actions) | `mvn verify` / ESLint / ビルド |
| インフラ | 本番デプロイ構成 (Nginx, TLS, サービス化) | — |
| 性能 | Redis 分散キャッシュへの切替 | Spring Cache の差替で対応可 |
| ストレージ | S3 互換 BLOB ストレージ | Spring Content の差替で対応可 |
| 連携 | Kafka / RabbitMQ 等の外部メッセージブローカ | Spring Modulith Externalization で対応 |
| 監視 | Prometheus + Grafana + Alertmanager | Micrometer 経由 |
| 構成 | auth-server 別プロセス化 (Microservice 分割) | 現状はモノリス同居で十分 |

---

## 11. オープン課題・後で決めること

| # | 項目 | 補足 |
| --- | --- | --- |
| O-1 | RSA 鍵の管理方式 (`auth-server`) | 開発: ファイル / 本番: KMS or HSM (将来) |
| O-2 | パスワードポリシーの初期値 | ABP 既定値を踏襲予定 |
| O-3 | ロールスコープ (`tenant_id` 単位 vs グローバル) | ABP は両方を許可、Java 版は最低でもテナント単位 |
| O-4 | Hibernate Envers の保持期間 | 大量化時の対応は将来課題 |
| O-5 | i18n リソース形式 (JSON → properties or `.po`) | 既存資産変換時に決定 |
| O-6 | フロント `Setting` UI の構造 | M5 着手時に Ant Design `ProForm` で詳細化 |

---

## 12. 参考資料

- 既存 .NET 版 README: [`../../../../AnomalyDetection/README.md`](../../../../AnomalyDetection/README.md)
- 既存 .NET 版ソリューション: [`../../../../AnomalyDetection/AnomalyDetection.sln`](../../../../AnomalyDetection/AnomalyDetection.sln)
- ABP Framework: <https://abp.io/docs>
- Spring Boot 3.3 Reference: <https://docs.spring.io/spring-boot/index.html>
- Spring Modulith Reference: <https://docs.spring.io/spring-modulith/reference/>
- Spring Authorization Server: <https://docs.spring.io/spring-authorization-server/reference/>
- Liquibase: <https://docs.liquibase.com/>
- MariaDB4j: <https://github.com/MariaDB4j/MariaDB4j>
- Ant Design: <https://ant.design/components/overview>

---

## 改訂履歴

| 日付 | バージョン | 変更内容 | 著者 |
| --- | --- | --- | --- |
| 2026-04-25 | 0.1 (Draft) | 初版。ブレインストーミング合意内容を spec 化 | ryuu1kyou + Claude Code |
